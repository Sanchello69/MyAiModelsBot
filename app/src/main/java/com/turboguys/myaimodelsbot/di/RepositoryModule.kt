package com.turboguys.myaimodelsbot.di

import com.turboguys.myaimodelsbot.data.repository.ChatRepositoryImpl
import com.turboguys.myaimodelsbot.domain.repository.ChatRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ChatRepository> {
        ChatRepositoryImpl(get())
    }
}
