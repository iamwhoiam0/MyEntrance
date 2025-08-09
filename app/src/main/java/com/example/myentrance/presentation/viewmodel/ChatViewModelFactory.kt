package com.example.myentrance.presentation.viewmodel


import com.example.myentrance.data.repository.ChatRepositoryImpl
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient


class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, authRepository, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun ProvideChatRepository(
    context: Context,
    supabaseClient: SupabaseClient,
    buildingId: String
): ChatRepository {
    return ChatRepositoryImpl(
        supabaseClient = supabaseClient,
        context = context,
        buildingId = buildingId
    )
}