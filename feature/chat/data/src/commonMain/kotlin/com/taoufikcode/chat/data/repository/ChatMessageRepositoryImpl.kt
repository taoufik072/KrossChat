package com.taoufikcode.chat.data.repository

import com.taoufikcode.chat.data.dto.websocket.OutgoingWebSocketDto
import com.taoufikcode.chat.data.dto.websocket.WebSocketMessageDto
import com.taoufikcode.chat.data.mappers.toDomain
import com.taoufikcode.chat.data.mappers.toEntity
import com.taoufikcode.chat.data.mappers.toWebSocketDto
import com.taoufikcode.chat.data.network.KtorWebSocketConnector
import com.taoufikcode.chat.data.services.ChatRemoteDataSource
import com.taoufikcode.chat.data.services.ChatRemoteDataSource.Companion.PAGE_SIZE
import com.taoufikcode.chat.database.KrossChatDatabase
import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.ChatMessageDeliveryStatus
import com.taoufikcode.chat.domain.models.MessageWithSender
import com.taoufikcode.chat.domain.models.OutgoingNewMessage
import com.taoufikcode.chat.domain.repository.ChatMessageRepository
import com.taoufikcode.core.data.database.safeDatabaseUpdate
import com.taoufikcode.core.domain.auth.SessionStorage
import com.taoufikcode.core.domain.util.DataError
import com.taoufikcode.core.domain.util.EmptyResult
import com.taoufikcode.core.domain.util.Result
import com.taoufikcode.core.domain.util.map
import com.taoufikcode.core.domain.util.onFailure
import com.taoufikcode.core.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ChatMessageRepositoryImpl(
    private val chatLocalDataBase: KrossChatDatabase,
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val webSocketConnector: KtorWebSocketConnector,
    private val json: Json,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope
) : ChatMessageRepository {

    override suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val dto = message.toWebSocketDto()

            val localUser = sessionStorage.observeAuthInfo().first()?.user
                ?: return Result.Failure(DataError.Local.NOT_FOUND)

            val entity = dto.toEntity(
                senderId = localUser.id,
                deliveryStatus = ChatMessageDeliveryStatus.SENDING
            )
            chatLocalDataBase.messageDao.upsertMessage(entity)

            return webSocketConnector
                .sendMessage(dto.toJsonPayload())
                .onFailure { error ->
                    applicationScope.launch {
                        chatLocalDataBase.messageDao.upsertMessage(
                            dto.toEntity(
                                senderId = localUser.id,
                                deliveryStatus = ChatMessageDeliveryStatus.FAILED
                            )
                        )
                    }.join()
                }
        }
    }


    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError> {
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

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return chatLocalDataBase
            .messageDao
            .getMessagesByChatId(chatId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
    }
    private fun OutgoingWebSocketDto.NewMessage.toJsonPayload(): String {
        val webSocketMessage = WebSocketMessageDto(
            type = type.name,
            payload = json.encodeToString(this)
        )
        return json.encodeToString(webSocketMessage)
    }
}
