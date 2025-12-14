package com.turboguys.myaimodelsbot

import android.app.Application
import com.turboguys.myaimodelsbot.di.databaseModule
import com.turboguys.myaimodelsbot.di.domainModule
import com.turboguys.myaimodelsbot.di.networkModule
import com.turboguys.myaimodelsbot.di.presentationModule
import com.turboguys.myaimodelsbot.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AiChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AiChatApplication)
            modules(
                databaseModule,
                networkModule,
                repositoryModule,
                domainModule,
                presentationModule
            )
        }
    }
}
