package com.example.myentrance.domain.repository

import android.net.Uri
import com.example.myentrance.domain.entities.News
import com.example.myentrance.domain.entities.Result

interface NewsRepository {
    suspend fun getNewsFeed(): List<News>
    suspend fun addNews(news: News, imageUri: Uri?): Result<Unit>
}


