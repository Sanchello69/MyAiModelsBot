package com.turboguys.myaimodelsbot.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val compressHistoryUseCase: CompressHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnMessageChange -> {
                _uiState.update { it.copy(inputText = event.message) }
            }

            ChatEvent.OnSendMessage -> {
                sendMessage()
            }

            is ChatEvent.OnModelSelect -> {
                _uiState.update {
                    it.copy(
                        selectedModel = event.model,
                        messages = emptyList() // Очистка истории при смене модели
                    )
                }
            }

            is ChatEvent.OnMaxTokensChange -> {
                _uiState.update { it.copy(maxTokens = event.maxTokens) }
            }

            is ChatEvent.OnCompressionToggle -> {
                _uiState.update { it.copy(compressionEnabled = event.enabled) }
            }

            ChatEvent.OnErrorDismiss -> {
                _uiState.update { it.copy(error = null) }
            }
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
            // Используем текущую историю для отправки
            val messagesToSend = _uiState.value.messages

            val result = sendMessageUseCase(
                model = currentState.selectedModel,
                messages = messagesToSend,
                maxTokens = if (currentState.maxTokens == 0) null else currentState.maxTokens
            )

            result.fold(
                onSuccess = { assistantMessage ->
                    // Добавляем ответ ассистента
                    val updatedMessages = _uiState.value.messages + assistantMessage

                    // Применяем сжатие к обновленной истории, если включено
                    val finalMessages = if (currentState.compressionEnabled && updatedMessages.size >= 10) {
                        compressHistoryUseCase(
                            model = currentState.selectedModel,
                            messages = updatedMessages
                        )
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
