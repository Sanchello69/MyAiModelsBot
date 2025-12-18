package com.turboguys.myaimodelsbot.di

import androidx.room.Room
import com.turboguys.myaimodelsbot.data.local.ChatLocalDataSource
import com.turboguys.myaimodelsbot.data.local.UserPreferencesManager
import com.turboguys.myaimodelsbot.data.local.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "ai_chat_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get<AppDatabase>().messageDao()
    }

    single {
        get<AppDatabase>().bitcoinPriceDao()
    }

    single {
        ChatLocalDataSource(get())
    }

    single {
        UserPreferencesManager(androidContext())
    }
}
