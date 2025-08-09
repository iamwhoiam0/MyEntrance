package com.example.myentrance.data.repository

import android.content.Context
import android.net.Uri
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient,
    private val context: Context
) : ProfileRepository {

    override suspend fun getUser(userId: String): Result<User> = try {
        val docSnapshot = firestore.collection("users").document(userId).get().await()
        if (docSnapshot.exists()) {
            val user = docSnapshot.toObject(User::class.java)
            if (user != null) Result.Success(user)
            else Result.Failure(Exception("Ошибка анализа пользователя"))
        } else Result.Failure(Exception("Пользователь не найден"))
    } catch (e: Exception) {
        Result.Failure(e)
    }

    override suspend fun updateUser(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    override suspend fun uploadAvatar(uri: Uri, userId: String): Result<String> = try {
        val fileName = "avatars/$userId.jpg"
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return Result.Failure(Exception("Невозможно открыть входной поток"))

        val bytes = inputStream.readBytes()
        inputStream.close()

        supabaseClient.storage.from("avatars").upload(fileName, bytes) {
            upsert = true
        }

        val publicUrl = supabaseClient.storage.from("avatars")
            .publicUrl(fileName) + "?t=${System.currentTimeMillis()}"

        Result.Success(publicUrl)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    override suspend fun updateUserAvatar(userId: String, avatarUrl: String): Result<Unit> = try {
        firestore.collection("users").document(userId)
            .update("avatarUrl", avatarUrl)
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(e)
    }
}