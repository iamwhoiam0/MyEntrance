package com.example.myentrance.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myentrance.data.repository.NewsRepositoryImpl
import com.example.myentrance.domain.entities.News
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.NewsRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.myentrance.domain.entities.Result
class NewsViewModel(
    private val newsRepository: NewsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _newsFeed = MutableStateFlow<List<News>>(emptyList())
    val newsFeed: StateFlow<List<News>> = _newsFeed.asStateFlow()

    private val _addNewsResult = MutableSharedFlow<Result<Unit>>()
    val addNewsResult = _addNewsResult.asSharedFlow()

    fun loadNews() {
        viewModelScope.launch {
            val newsList = newsRepository.getNewsFeed()
            _newsFeed.value = newsList
        }
    }

    fun addNews(text: String, imageUri: Uri?) {
        val user = authRepository.getCurrentUser() ?: return
        val news = News(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            imageUrl = null,
            createdAt = System.currentTimeMillis(),
            userId = user.id,
            userName = user.name
        )
        viewModelScope.launch {
            val result = newsRepository.addNews(news, imageUri)
            _addNewsResult.emit(result)
            if (result is Result.Success) {
                loadNews()
            }
        }
    }
}

fun ProvideNewsRepository(
    context: Context,
    supabaseClient: SupabaseClient
): NewsRepository {
    return NewsRepositoryImpl(supabaseClient, context)
}

class NewsViewModelFactory(
    private val newsRepository: NewsRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(newsRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
