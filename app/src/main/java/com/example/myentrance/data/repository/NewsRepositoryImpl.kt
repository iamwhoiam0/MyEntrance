package com.example.myentrance.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myentrance.domain.entities.News
import com.example.myentrance.domain.repository.NewsRepository
import io.github.jan.supabase.SupabaseClient
import com.example.myentrance.domain.entities.Result
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class NewsRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) : NewsRepository {

    override suspend fun getNewsFeed(): List<News> {
        return try {
            supabaseClient
                .from("news")
                .select {
                    order(column = "createdAt", order = Order.DESCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            Log.e("NewsRepositoryImpl", "Ошибка получения новостной ленты: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun addNews(news: News, imageUri: Uri?): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val fileName = "news_images/${news.id}_${System.currentTimeMillis()}.jpg"

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return Result.Failure(Exception("Не удалось открыть поток ввода"))

                val bytes = inputStream.readBytes()
                inputStream.close()

                supabaseClient.storage.from("news-photos").upload(fileName, bytes)
                imageUrl = supabaseClient.storage.from("news-photos").publicUrl(fileName)
            }

            val newsToInsert = news.copy(imageUrl = imageUrl)
            supabaseClient.from("news").insert(newsToInsert)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("NewsRepositoryImpl", "Ошибка при добавлении новости: ${e.message}", e)
            Result.Failure(e)
        }
    }
}