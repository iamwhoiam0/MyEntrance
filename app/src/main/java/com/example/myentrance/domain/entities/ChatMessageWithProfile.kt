package com.example.myentrance.domain.entities

data class ChatMessageWithProfile(
    val id: String = "",
    val senderId: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val senderName: String = "",
    val senderAvatarUrl: String? = null
)