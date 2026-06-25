package com.taoufikcode.chat.domain.service

import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.ChatMessageDeliveryStatus
import com.taoufikcode.chat.domain.models.ConnectionState
import com.taoufikcode.core.domain.util.DataError
import com.taoufikcode.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val connectionState: StateFlow<ConnectionState>
    val chatMessages: Flow<ChatMessage>
    suspend fun sendMessage(message: ChatMessage): EmptyResult<DataError.Connection>
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>
}