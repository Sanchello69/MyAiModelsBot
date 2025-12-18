package com.turboguys.myaimodelsbot.di

import com.turboguys.myaimodelsbot.data.remote.mcp.SimpleMcpClient
import com.turboguys.myaimodelsbot.data.repository.BitcoinRepositoryImpl
import com.turboguys.myaimodelsbot.data.repository.ChatRepositoryImpl
import com.turboguys.myaimodelsbot.domain.repository.BitcoinRepository
import com.turboguys.myaimodelsbot.domain.repository.ChatRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ChatRepository> {
        ChatRepositoryImpl(get())
    }

    single { SimpleMcpClient() }

    single<BitcoinRepository> {
        BitcoinRepositoryImpl(get(), get())
    }
}
