package com.example.myentrance.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myentrance.domain.entities.ChatMessage
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.repository.ChatRepository
import com.google.firebase.database.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val context: Context,
    buildingId: String
) : ChatRepository {

    private val firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance()
        .reference
        .child("chats")
        .child(buildingId)
        .child("messages")

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val key = firebaseDatabase.push().key
                ?: return Result.Failure(Exception("Ключ не сгенерирован"))
            val messageData = mapOf(
                "id" to key,
                "senderId" to message.senderId,
                "text" to message.text,
                "imageUrl" to message.imageUrl,
                "timestamp" to ServerValue.TIMESTAMP
            )
            firebaseDatabase.child(key).setValue(messageData).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Ошибка отправки сообщения", e)
            Result.Failure(e)
        }
    }

    override fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { ds ->
                    val id = ds.child("id").getValue(String::class.java) ?: return@mapNotNull null
                    val senderId = ds.child("senderId").getValue(String::class.java) ?: ""
                    val text = ds.child("text").getValue(String::class.java)
                    val imageUrl = ds.child("imageUrl").getValue(String::class.java)
                    val timestampAny = ds.child("timestamp").value
                    val timestamp = when (timestampAny) {
                        is Long -> timestampAny
                        is Int -> timestampAny.toLong()
                        is Double -> timestampAny.toLong()
                        else -> System.currentTimeMillis()
                    }
                    ChatMessage(
                        id = id,
                        senderId = senderId,
                        text = text,
                        imageUrl = imageUrl,
                        timestamp = timestamp
                    )
                }.sortedBy { it.timestamp }
                trySend(messages).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        firebaseDatabase.addValueEventListener(listener)
        awaitClose { firebaseDatabase.removeEventListener(listener) }
    }

    override suspend fun uploadAttachment(uri: Uri): Result<String> = try {
        val fileName = "chat_images/${UUID.randomUUID()}.jpg"
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: return Result.Failure(Exception("Не удалось открыть Stream"))

        supabaseClient.storage.from("chat-images").upload(fileName, bytes)
        val publicUrl = supabaseClient.storage.from("chat-images").publicUrl(fileName)
        Result.Success(publicUrl)
    } catch (e: Exception) {
        Result.Failure(e)
    }
}
