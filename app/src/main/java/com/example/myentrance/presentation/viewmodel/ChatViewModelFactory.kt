package com.example.myentrance.presentation.viewmodel


import ChatRepositoryImpl
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient


class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun ProvideChatRepository(
    context: Context,
    supabaseClient: SupabaseClient
): ChatRepository {
    return ChatRepositoryImpl(
        supabaseClient = supabaseClient,
        context = context
        // firebaseDatabase использую по умолчанию (нужно поправить думаю)
    )
}


