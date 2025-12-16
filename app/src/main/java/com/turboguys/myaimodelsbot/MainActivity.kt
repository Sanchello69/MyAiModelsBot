package com.turboguys.myaimodelsbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.turboguys.myaimodelsbot.presentation.chat.ChatScreen
import com.turboguys.myaimodelsbot.ui.theme.MyAiModelsBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Тест MCP подключения (проверьте Logcat для результатов)
        McpTestExample.testMcpConnection()

        setContent {
            MyAiModelsBotTheme {
                ChatScreen()
            }
        }
    }
}