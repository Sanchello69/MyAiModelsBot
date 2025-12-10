package com.turboguys.myaimodelsbot.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turboguys.myaimodelsbot.domain.model.Message
import com.turboguys.myaimodelsbot.domain.model.MessageRole
import com.turboguys.myaimodelsbot.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase
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
            val result = sendMessageUseCase(
                model = currentState.selectedModel,
                messages = _uiState.value.messages,
                maxTokens = currentState.maxTokens
            )

            result.fold(
                onSuccess = { assistantMessage ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + assistantMessage,
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
