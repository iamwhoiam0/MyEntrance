package com.example.myentrance.domain.entities

data class NewsWithUser(
    val news: News,
    val userName: String,
    val userAvatarUrl: String?
)
