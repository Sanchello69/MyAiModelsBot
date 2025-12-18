package com.turboguys.myaimodelsbot.di

import com.turboguys.myaimodelsbot.presentation.bitcoin.BitcoinMonitorViewModel
import com.turboguys.myaimodelsbot.presentation.chat.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        ChatViewModel(get(), get(), get(), get())
    }
    viewModel {
        BitcoinMonitorViewModel(get(), get())
    }
}
