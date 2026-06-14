package com.taoufikcode.chat.data.repository

import com.taoufikcode.chat.data.dto.websocket.IncomingWebSocketDto
import com.taoufikcode.chat.data.dto.websocket.WebSocketMessageDto
import com.taoufikcode.chat.data.mappers.toDomain
import com.taoufikcode.chat.data.mappers.toNewMessage
import com.taoufikcode.chat.data.network.IncomingMessageHandler
import com.taoufikcode.chat.data.network.KtorWebSocketConnector
import com.taoufikcode.chat.database.KrossChatDatabase
import com.taoufikcode.chat.domain.ChatMessageRepository
import com.taoufikcode.chat.domain.error.ConnectionError
import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.ChatMessageDeliveryStatus
import com.taoufikcode.core.data.database.safeDatabaseUpdate
import com.taoufikcode.core.domain.util.DataError
import com.taoufikcode.core.domain.util.EmptyResult
import com.taoufikcode.core.domain.util.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class ChatMessageRepositoryImpl(
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatLocalDataBase: KrossChatDatabase,
    private val json: Json,
    private val incomingMessageHandler: IncomingMessageHandler,
    applicationScope: CoroutineScope,
) : ChatMessageRepository {

    override val connectionState = webSocketConnector.connectionState

    override val chatMessages: Flow<ChatMessage> = webSocketConnector
        .messages
        .mapNotNull {
            //parse messageDto
            incomingMessageHandler.parse(it)
        }
        .onEach {
            incomingMessageHandler.handle(it)
        }
        .filterIsInstance<IncomingWebSocketDto.NewMessageDto>()

        .mapNotNull {
            chatLocalDataBase.messageDao.getMessageById(it.id)?.toDomain()
        }
        .shareIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5000)
        )

    override suspend fun sendMessage(message: ChatMessage): EmptyResult<ConnectionError> {
        val outgoingDto = message.toNewMessage()
        val webSocketMessage = WebSocketMessageDto(
            type = outgoingDto.type.name,
            payload = json.encodeToString(outgoingDto)
        )
        val rawJsonPayload = json.encodeToString(webSocketMessage)

        return webSocketConnector
            .sendMessage(rawJsonPayload)
            .onFailure { error ->
                updateMessageDeliveryStatus(
                    messageId = message.id,
                    status = ChatMessageDeliveryStatus.FAILED
                )
            }
    }

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            chatLocalDataBase.messageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

}