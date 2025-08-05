package com.example.myentrance.domain.entities

data class ChatMessage(
    val id: String = "",            // Уникальный ID сообщения (например, UUID или из базы)
    val senderId: String = "",      // UID пользователя (Firebase Auth)
    val senderName: String = "",    // Имя пользователя
    val text: String? = null,       // Текст сообщения (если есть)
    val imageUrl: String? = null,   // Ссылка на вложенное изображение (если есть)
    val timestamp: Long = System.currentTimeMillis() // Время отправки
)
