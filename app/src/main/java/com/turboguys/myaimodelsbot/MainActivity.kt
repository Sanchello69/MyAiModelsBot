package com.turboguys.myaimodelsbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.turboguys.myaimodelsbot.presentation.bitcoin.BitcoinMonitorScreen
import com.turboguys.myaimodelsbot.presentation.chat.ChatScreen
import com.turboguys.myaimodelsbot.ui.theme.MyAiModelsBotTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyAiModelsBotTheme {
                var selectedTab by remember { mutableStateOf(0) }
                val tabs = listOf("Chat", "Bitcoin Monitor")

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        when (selectedTab) {
                            0 -> ChatScreen()
                            1 -> BitcoinMonitorScreen()
                        }
                    }
                }
            }
        }
    }
}