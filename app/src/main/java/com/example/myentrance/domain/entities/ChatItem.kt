package com.example.myentrance.domain.entities

sealed class ChatItem {
    data class Message(val message: ChatMessageWithProfile) : ChatItem()
    data class DateSeparator(val date: String) : ChatItem()
}