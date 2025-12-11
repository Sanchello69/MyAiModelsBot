package com.turboguys.myaimodelsbot.di

import com.turboguys.myaimodelsbot.domain.usecase.CompressHistoryUseCase
import com.turboguys.myaimodelsbot.domain.usecase.SendMessageUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        SendMessageUseCase(get())
    }
    factory {
        CompressHistoryUseCase(get())
    }
}
