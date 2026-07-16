package com.taoufikcode.chat.data.repository

import com.taoufikcode.chat.data.dto.websocket.OutgoingWebSocketDto
import com.taoufikcode.chat.data.dto.websocket.WebSocketMessageDto
import com.taoufikcode.chat.data.mappers.toDomain
import com.taoufikcode.chat.data.mappers.toEntity
import com.taoufikcode.chat.data.mappers.toWebSocketDto
import com.taoufikcode.chat.data.network.GeminiClient
import com.taoufikcode.chat.data.network.GeminiContent
import com.taoufikcode.chat.data.network.GeminiPart
import com.taoufikcode.chat.data.network.KtorWebSocketConnector
import com.taoufikcode.chat.data.services.ChatRemoteDataSource
import com.taoufikcode.chat.data.services.ChatRemoteDataSource.Companion.PAGE_SIZE
import com.taoufikcode.chat.database.KrossChatDatabase
import com.taoufikcode.chat.database.entities.MessageEntity
import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.ChatMessageDeliveryStatus
import com.taoufikcode.chat.domain.models.MessageWithSender
import com.taoufikcode.chat.domain.models.OutgoingNewMessage
import com.taoufikcode.chat.domain.repository.ChatMessageRepository
import com.taoufikcode.core.data.database.safeDatabaseUpdate
import com.taoufikcode.core.domain.auth.SessionStorage
import com.taoufikcode.core.domain.logging.KrossChatLogger
import com.taoufikcode.core.domain.util.DataError
import com.taoufikcode.core.domain.util.EmptyResult
import com.taoufikcode.core.domain.util.Result
import com.taoufikcode.core.domain.util.map
import com.taoufikcode.core.domain.util.onFailure
import com.taoufikcode.core.domain.util.onSuccess
import com.taoufikcode.feature.chat.data.BuildKonfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.onSuccess
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatMessageRepositoryImpl(
    private val chatLocalDataBase: KrossChatDatabase,
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val webSocketConnector: KtorWebSocketConnector,
    private val json: Json,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
    private val geminiClient: GeminiClient,
    private val logger: KrossChatLogger
) : ChatMessageRepository {

    override suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val dto = message.toWebSocketDto()

            val localUser = sessionStorage.observeAuthInfo().first()?.user
                ?: return Result.Failure(DataError.Local.NOT_FOUND)

            if (message.chatId == "gemini_chat") {
                logger.i("ChatMessageRepositoryImpl") { "Intercepted message for gemini_chat. MessageId: ${message.messageId}" }
                val entity = dto.toEntity(
                    senderId = localUser.id,
                    deliveryStatus = ChatMessageDeliveryStatus.SENT
                )
                chatLocalDataBase.messageDao.upsertMessage(entity)
                chatLocalDataBase.chatDao.updateLastActivityAt("gemini_chat", Clock.System.now().toEpochMilliseconds())

                applicationScope.launch {
                    generateGeminiResponse(message.content)
                }

                return Result.Success(Unit)
            }

            val entity = dto.toEntity(
                senderId = localUser.id,
                deliveryStatus = ChatMessageDeliveryStatus.SENDING
            )
            chatLocalDataBase.messageDao.upsertMessage(entity)

            return webSocketConnector
                .sendMessage(dto.toJsonPayload())
                .onFailure { error ->
                    applicationScope.launch {
                        chatLocalDataBase.messageDao.updateDeliveryStatus(
                            messageId = entity.messageId,
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            status = ChatMessageDeliveryStatus.FAILED.name
                        )
                    }.join()
                }
        }
    }


    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError> {
        if (chatId == "gemini_chat") {
            return Result.Success(emptyList())
        }
        return chatRemoteDataSource
            .fetchMessages(chatId, before)
            .map { messageDto -> messageDto.map { it.toDomain() } }
            .onSuccess { messages ->
                return safeDatabaseUpdate {
                    chatLocalDataBase.messageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = messages.map { it.toEntity() },
                        pageSize = PAGE_SIZE,
                        shouldSync = before == null // Only sync for most recent page
                    )
                    messages
                }
            }
    }

    override suspend fun retryMessage(messageId: String): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val message = chatLocalDataBase.messageDao.getMessageById(messageId)
                ?: return Result.Failure(DataError.Local.NOT_FOUND)


            chatLocalDataBase.messageDao.updateDeliveryStatus(
                messageId = messageId,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                status = ChatMessageDeliveryStatus.SENDING.name
            )

            val outgoingNewMessage = OutgoingWebSocketDto.NewMessage(
                chatId = message.chatId,
                messageId = messageId,
                content = message.content
            )
            return webSocketConnector
                .sendMessage(outgoingNewMessage.toJsonPayload())
                .onFailure {
                    applicationScope.launch {
                        chatLocalDataBase.messageDao.upsertMessage(
                            message.copy(
                                deliveryStatus = ChatMessageDeliveryStatus.FAILED.name,
                                timestamp = Clock.System.now().toEpochMilliseconds()
                            )
                        )
                    }.join()
                }
        }
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return chatLocalDataBase
            .messageDao
            .getMessagesByChatId(chatId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
    }
    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        return chatRemoteDataSource
            .deleteMessage(messageId)
            .onSuccess {
                applicationScope.launch {
                    chatLocalDataBase.messageDao.deleteMessageById(messageId)
                }.join()
            }
    }
    private fun OutgoingWebSocketDto.NewMessage.toJsonPayload(): String {
        val webSocketMessage = WebSocketMessageDto(
            type = type.name,
            payload = json.encodeToString(this)
        )
        return json.encodeToString(webSocketMessage)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun generateGeminiResponse(userPrompt: String) {
        logger.i("ChatMessageRepositoryImpl") { "generateGeminiResponse start. Prompt: $userPrompt" }
        val apiKey = BuildKonfig.GEMINI_API_KEY
        val responseMessageId = Uuid.random().toString()
        val now = Clock.System.now().toEpochMilliseconds()

        if (apiKey.isBlank()) {
            logger.w("ChatMessageRepositoryImpl") { "Gemini API Key is blank! Cancelling request." }
            val errorEntity = MessageEntity(
                messageId = responseMessageId,
                chatId = "gemini_chat",
                senderId = "gemini_bot",
                content = "Please configure GEMINI_API_KEY in your local.properties file.",
                timestamp = now,
                deliveryStatus = ChatMessageDeliveryStatus.SENT.name,
                deliveryStatusTimestamp = now
            )
            chatLocalDataBase.messageDao.upsertMessage(errorEntity)
            chatLocalDataBase.chatDao.updateLastActivityAt("gemini_chat", now)
            return
        }

        // Insert placeholder message
        logger.d("ChatMessageRepositoryImpl") { "API Key is configured. Inserting placeholder message..." }
        val placeholderEntity = MessageEntity(
            messageId = responseMessageId,
            chatId = "gemini_chat",
            senderId = "gemini_bot",
            content = "...",
            timestamp = now,
            deliveryStatus = ChatMessageDeliveryStatus.SENT.name,
            deliveryStatusTimestamp = now
        )
        chatLocalDataBase.messageDao.upsertMessage(placeholderEntity)
        chatLocalDataBase.chatDao.updateLastActivityAt("gemini_chat", now)

        // Load recent messages for context
        logger.d("ChatMessageRepositoryImpl") { "Loading context history from local database..." }
        val historyEntities = chatLocalDataBase.messageDao.getMessagesByChatIdLimited("gemini_chat", 20).first().reversed()
        logger.d("ChatMessageRepositoryImpl") { "Loaded ${historyEntities.size} context messages." }

        // Format conversation history for Gemini API
        val history = historyEntities.map { message ->
            val role = if (message.senderId == "gemini_bot") "model" else "user"
            GeminiContent(
                role = role,
                parts = listOf(GeminiPart(text = message.content))
            )
        }

        var accumulatedContent = ""
        try {
            logger.i("ChatMessageRepositoryImpl") { "Calling generateResponseStream..." }
            geminiClient.generateResponseStream(apiKey, history).collect { chunk ->
                if (accumulatedContent == "...") {
                    accumulatedContent = ""
                }
                accumulatedContent += chunk
                chatLocalDataBase.messageDao.updateMessageContent(responseMessageId, accumulatedContent)
            }
            logger.i("ChatMessageRepositoryImpl") { "Streaming finished successfully." }
        } catch (e: Exception) {
            logger.e("ChatMessageRepositoryImpl", e) { "Error generating/collecting Gemini response: ${e.message}" }
            chatLocalDataBase.messageDao.updateMessageContent(responseMessageId, "Error: Unable to stream response.")
        }
    }
}
