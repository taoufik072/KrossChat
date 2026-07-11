package com.taoufikcode.krosschat

import android.app.Application
import com.taoufikcode.krosschat.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KrossChatApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@KrossChatApplication)
            androidLogger()

        }
    }
}