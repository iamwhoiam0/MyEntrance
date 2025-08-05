package com.example.myentrance.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class News(
    val id: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "",
    val userName: String = ""
)
