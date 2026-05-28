package com.taoufikcode.chat.presentation.chat_list.di

import com.taoufikcode.chat.presentation.chat_list.ChatListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatPresentationModule = module {
    singleOf(::ChatListViewModel)
}