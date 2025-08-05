package com.example.myentrance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myentrance.domain.entities.ChatMessage
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.myentrance.domain.entities.Result
import android.net.Uri
import android.util.Log
import com.example.myentrance.domain.repository.AuthRepository

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _sendMessageResult = MutableSharedFlow<Result<Unit>>()
    val sendMessageResult = _sendMessageResult.asSharedFlow()

    private val _uploadAttachmentResult = MutableSharedFlow<Result<String>>()
    val uploadAttachmentResult = _uploadAttachmentResult.asSharedFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages()
                .catch { e ->
                    // добавить лог ошибки
                }
                .collect { messagesList ->
                    _messages.value = messagesList
                }
        }
    }

    fun sendMessage(text: String, imageUrl: String? = null) {
        val user = authRepository.getCurrentUser() ?: return
        val message = ChatMessage(
            id = "",
            senderId = user.id,
            senderName = user.name,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val result = chatRepository.sendMessage(message)
            if (result is Result.Failure) {
                Log.e("ChatViewModel", "Send error: ${result.exception.message}")
            }
            _sendMessageResult.emit(result)
        }
    }

    fun uploadAttachment(uri: Uri) {
        viewModelScope.launch {
            val result = chatRepository.uploadAttachment(uri)
            _uploadAttachmentResult.emit(result)
        }
    }
}





