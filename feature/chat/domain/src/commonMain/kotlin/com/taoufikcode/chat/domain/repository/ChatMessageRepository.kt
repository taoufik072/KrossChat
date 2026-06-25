package com.taoufikcode.chat.domain.repository

import com.taoufikcode.chat.domain.models.ChatMessage
import com.taoufikcode.chat.domain.models.MessageWithSender
import com.taoufikcode.chat.domain.models.OutgoingNewMessage
import com.taoufikcode.core.domain.util.DataError
import com.taoufikcode.core.domain.util.EmptyResult
import com.taoufikcode.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError>

    fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>>
    suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError>
}