package com.turboguys.myaimodelsbot.di

import com.turboguys.myaimodelsbot.domain.usecase.AnalyzeBitcoinChangeUseCase
import com.turboguys.myaimodelsbot.domain.usecase.AnalyzeBitcoinWithToolsUseCase
import com.turboguys.myaimodelsbot.domain.usecase.CompressHistoryUseCase
import com.turboguys.myaimodelsbot.domain.usecase.SendMessageUseCase
import com.turboguys.myaimodelsbot.domain.usecase.SendMessageWithToolsUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        SendMessageUseCase(get())
    }
    factory {
        SendMessageWithToolsUseCase(get(), get())
    }
    factory {
        CompressHistoryUseCase(get())
    }
    factory {
        AnalyzeBitcoinChangeUseCase(get())
    }
    factory {
        AnalyzeBitcoinWithToolsUseCase(get(), get(), get())
    }
}
