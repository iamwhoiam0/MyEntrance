package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Comment

interface CommentRepository {
    suspend fun getComments(discussionId: String): List<Comment>
    suspend fun addComment(comment: Comment): Boolean
    suspend fun deleteComment(commentId: String): Boolean
    suspend fun updateComment(comment: Comment): Boolean
    suspend fun getReplies(parentCommentId: String): List<Comment>
}