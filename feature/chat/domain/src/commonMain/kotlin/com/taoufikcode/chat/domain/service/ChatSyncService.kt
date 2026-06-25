package com.taoufikcode.chat.domain.service

import com.taoufikcode.chat.domain.models.Chat

interface ChatSyncService {
    suspend fun refreshChat(chatId: String)
    suspend fun cacheChat(chat: Chat)
}