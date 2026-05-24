package com.taoufikcode.core.data.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCoreModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
}