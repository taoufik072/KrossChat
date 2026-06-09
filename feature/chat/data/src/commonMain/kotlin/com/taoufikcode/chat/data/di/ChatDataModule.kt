package com.taoufikcode.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.taoufikcode.chat.data.remote.ChatRemoteDataSource
import com.taoufikcode.chat.data.repository.ChatRepositoryImpl
import com.taoufikcode.chat.database.DatabaseFactory
import com.taoufikcode.chat.domain.ChatRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module
val chatDataModule = module {
    includes(platformChatDataModule)
    singleOf(::ChatRepositoryImpl) bind ChatRepository::class
    singleOf(::ChatRemoteDataSource)
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}