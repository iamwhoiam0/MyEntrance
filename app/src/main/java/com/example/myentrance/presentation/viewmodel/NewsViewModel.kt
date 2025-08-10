package com.example.myentrance.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myentrance.domain.entities.News
import com.example.myentrance.domain.entities.NewsWithUser
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.NewsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.myentrance.domain.entities.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _newsFeed = MutableStateFlow<List<NewsWithUser>>(emptyList())
    val newsFeed: StateFlow<List<NewsWithUser>> = _newsFeed.asStateFlow()

    private val _addNewsResult = MutableSharedFlow<Result<Unit>>()
    val addNewsResult = _addNewsResult.asSharedFlow()

    fun loadNews() {
        viewModelScope.launch {
            val newsList = newsRepository.getNewsFeedWithUser()
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