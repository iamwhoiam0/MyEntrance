package com.example.myentrance.domain.entities

data class Discussion(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val buildingId: String,
    val category: String,
    val isPinned: Boolean,
    val status: DiscussionStatus,
    val commentsCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)