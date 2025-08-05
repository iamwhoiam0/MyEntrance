package com.example.myentrance.domain.repository

import android.net.Uri
import com.example.myentrance.domain.entities.ChatMessage
import kotlinx.coroutines.flow.Flow
import com.example.myentrance.domain.entities.Result

interface ChatRepository {
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun uploadAttachment(uri: Uri): Result<String> // выдаёт ссылку на файл в бд supabase
}
