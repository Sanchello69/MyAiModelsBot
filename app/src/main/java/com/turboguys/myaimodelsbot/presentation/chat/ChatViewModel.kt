package com.turboguys.myaimodelsbot.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turboguys.myaimodelsbot.data.local.ChatLocalDataSource
import com.turboguys.myaimodelsbot.data.local.UserPreferencesManager
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.MessageRole
import com.turboguys.myaimodelsbot.domain.usecase.CompressHistoryUseCase
import com.turboguys.myaimodelsbot.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val compressHistoryUseCase: CompressHistoryUseCase,
    private val localDataSource: ChatLocalDataSource,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadMessages()
    }

    private fun loadPreferences() {
        _uiState.update {
            it.copy(
                selectedModel = preferencesManager.getSelectedModel(),
                maxTokens = preferencesManager.getMaxTokens(),
                compressionEnabled = preferencesManager.getCompressionEnabled()
            )
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val messages = localDataSource.getAllMessagesSync()
            _uiState.update { it.copy(messages = messages) }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnMessageChange -> {
                _uiState.update { it.copy(inputText = event.message) }
            }

            ChatEvent.OnSendMessage -> {
                sendMessage()
            }

            is ChatEvent.OnModelSelect -> {
                viewModelScope.launch {
                    preferencesManager.saveSelectedModel(event.model)
                    localDataSource.clearAllMessages()
                    _uiState.update {
                        it.copy(
                            selectedModel = event.model,
                            messages = emptyList()
                        )
                    }
                }
            }

            is ChatEvent.OnMaxTokensChange -> {
                preferencesManager.saveMaxTokens(event.maxTokens)
                _uiState.update { it.copy(maxTokens = event.maxTokens) }
            }

            is ChatEvent.OnCompressionToggle -> {
                preferencesManager.saveCompressionEnabled(event.enabled)
                _uiState.update { it.copy(compressionEnabled = event.enabled) }
            }

            ChatEvent.OnClearHistory -> {
                clearHistory()
            }

            ChatEvent.OnErrorDismiss -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            localDataSource.clearAllMessages()
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    private fun sendMessage() {
        val currentState = _uiState.value
        val messageText = currentState.inputText.trim()

        if (messageText.isEmpty() || currentState.isLoading) return

        val userMessage = Message(
            role = MessageRole.USER,
            content = messageText
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            // Сохраняем сообщение пользователя в БД
            localDataSource.saveMessage(userMessage)

            // Используем текущую историю для отправки
            val messagesToSend = _uiState.value.messages

            val result = sendMessageUseCase(
                model = currentState.selectedModel,
                messages = messagesToSend,
                maxTokens = if (currentState.maxTokens == 0) null else currentState.maxTokens
            )

            result.fold(
                onSuccess = { assistantMessage ->
                    // Сохраняем ответ ассистента в БД
                    localDataSource.saveMessage(assistantMessage)

                    // Добавляем ответ ассистента
                    val updatedMessages = _uiState.value.messages + assistantMessage

                    // Применяем сжатие к обновленной истории, если включено
                    val finalMessages = if (currentState.compressionEnabled && updatedMessages.size >= 10) {
                        val compressed = compressHistoryUseCase(
                            model = currentState.selectedModel,
                            messages = updatedMessages
                        )
                        // Сохраняем сжатую историю в БД
                        localDataSource.clearAllMessages()
                        localDataSource.saveMessages(compressed)
                        compressed
                    } else {
                        updatedMessages
                    }

                    _uiState.update {
                        it.copy(
                            messages = finalMessages,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred"
                        )
                    }
                }
            )
        }
    }
}
