package com.example.myentrance.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myentrance.data.repository.ChatRepositoryImpl
import com.example.myentrance.domain.entities.ChatMessage
import com.example.myentrance.domain.entities.ChatMessageWithProfile
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel @AssistedInject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient,
    @Assisted private val buildingId: String,
    private val application: Application // обычная зависимость, НЕ @Assisted
) : ViewModel() {

    private val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(supabaseClient, application, buildingId)
    }

    private val _sendMessageResult: MutableSharedFlow<Result<Unit>> = MutableSharedFlow()
    val sendMessageResult = _sendMessageResult.asSharedFlow()

    private val _messages: MutableStateFlow<List<ChatMessageWithProfile>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<ChatMessageWithProfile>> = _messages.asStateFlow()

    private val userCache = mutableMapOf<String, User>()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages().collect { messagesList ->
                val updated = messagesList.map { msg ->
                    val userData = userCache[msg.senderId] ?: firestore
                        .collection("users")
                        .document(msg.senderId)
                        .get()
                        .await()
                        .toObject(User::class.java)
                        ?.also { userCache[msg.senderId] = it }

                    ChatMessageWithProfile(
                        id = msg.id,
                        senderId = msg.senderId,
                        text = msg.text,
                        imageUrl = msg.imageUrl,
                        timestamp = msg.timestamp,
                        senderName = userData?.name ?: "",
                        senderAvatarUrl = userData?.avatarUrl
                    )
                }
                _messages.value = updated
            }
        }
    }

    fun sendMessage(text: String, imageUrl: String? = null) {
        val user = authRepository.getCurrentUser() ?: return
        val message = ChatMessage(
            senderId = user.id,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val result = chatRepository.sendMessage(message)
            _sendMessageResult.emit(result)
        }
    }
}

@AssistedFactory
interface ChatViewModelFactory {
    fun create(buildingId: String): ChatViewModel
}

