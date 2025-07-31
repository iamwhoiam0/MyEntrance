package com.example.myentrance.domain.entities

data class Comment(
    val id: String,
    val discussionId: String,
    val authorId: String,
    val content: String,
    val attachments: List<Attachment>,
    val createdAt: Long,
    val parentCommentId: String?, // nullable, если корневой комментарий
    val status: CommentStatus
)