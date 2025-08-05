import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myentrance.domain.entities.ChatMessage
import com.example.myentrance.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.myentrance.domain.entities.Result
import io.github.jan.supabase.storage.storage

class ChatRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val context: Context,
    private val firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("chats/messages")
) : ChatRepository {


    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            Log.d("ChatRepo", "sendMessage: $message")
            val key = firebaseDatabase.push().key ?: return Result.Failure(Exception("No key"))
            val messageData = mapOf(
                "id" to key,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "text" to message.text,
                "imageUrl" to message.imageUrl,
                "timestamp" to ServerValue.TIMESTAMP
            )
            firebaseDatabase.child(key).setValue(messageData).await()
            Log.d("ChatRepo", "Message sent!")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending message: ${e.message}")
            Result.Failure(e)
        }
    }


    override fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { ds ->
                    val id = ds.child("id").getValue(String::class.java) ?: return@mapNotNull null
                    val senderId = ds.child("senderId").getValue(String::class.java) ?: ""
                    val senderName = ds.child("senderName").getValue(String::class.java) ?: ""
                    val text = ds.child("text").getValue(String::class.java)
                    val imageUrl = ds.child("imageUrl").getValue(String::class.java)
                    val timestamp = ds.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                    ChatMessage(id, senderId, senderName, text, imageUrl, timestamp)
                }.sortedBy { it.timestamp }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        firebaseDatabase.addValueEventListener(listener)
        awaitClose { firebaseDatabase.removeEventListener(listener) }
    }

    override suspend fun uploadAttachment(uri: Uri): Result<String> {
        return try {
            val fileName = "chat_images/${UUID.randomUUID()}.jpg"
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.Failure(Exception("Unable to open input stream"))
            val bytes = inputStream.readBytes()
            inputStream.close()

            supabaseClient.storage.from("chat-images").upload(fileName, bytes)

            val publicUrl = supabaseClient.storage.from("chat-images").publicUrl(fileName)
            Result.Success(publicUrl)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}
