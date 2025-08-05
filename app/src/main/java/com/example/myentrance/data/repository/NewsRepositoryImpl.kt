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
import io.github.jan.supabase.storage.upload
import java.io.File

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
                .decodeList<News>()
        } catch (e: Exception) {
            Log.e("newsRepImpl", e.message.toString())
            emptyList()
        }
    }

    override suspend fun addNews(news: News, imageUri: Uri?): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val fileName = "news_images/${news.id}.jpg"
                val file = File(getRealPathFromUri(context, imageUri))
                supabaseClient.storage.from("news-photos").upload(fileName, file)
                imageUrl = supabaseClient.storage.from("news-photos").publicUrl(fileName)
            }
            val newsToInsert = news.copy(imageUrl = imageUrl)
            supabaseClient.from("news").insert(newsToInsert)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }


    private fun getRealPathFromUri(context: Context, uri: Uri): String {
        // Нужно будет реализовать получение реального пути к файлу из Uri приложения
        throw NotImplementedError("Implement URI to file path conversion")
    }
}
