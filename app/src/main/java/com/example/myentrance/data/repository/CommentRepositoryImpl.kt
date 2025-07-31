package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Attachment
import com.example.myentrance.domain.entities.Comment
import com.example.myentrance.domain.entities.CommentStatus
import com.example.myentrance.domain.repository.CommentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CommentRepository {

    override suspend fun getComments(discussionId: String): List<Comment> {
        val snap = firestore.collection("comments")
            .whereEqualTo("discussionId", discussionId)
            .get().await()
        return snap.documents.mapNotNull { it.toComment() }
    }

    override suspend fun addComment(comment: Comment): Boolean {
        firestore.collection("comments")
            .document(comment.id)
            .set(comment.toMap())
            .await()
        return true
    }

    override suspend fun deleteComment(commentId: String): Boolean {
        firestore.collection("comments").document(commentId).delete().await()
        return true
    }

    override suspend fun updateComment(comment: Comment): Boolean {
        firestore.collection("comments")
            .document(comment.id)
            .update(comment.toMap())
            .await()
        return true
    }

    override suspend fun getReplies(parentCommentId: String): List<Comment> {
        val snap = firestore.collection("comments")
            .whereEqualTo("parentCommentId", parentCommentId)
            .get().await()
        return snap.documents.mapNotNull { it.toComment() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toComment(): Comment? {
        return try {
            Comment(
                id = id,
                discussionId = getString("discussionId") ?: "",
                authorId = getString("authorId") ?: "",
                content = getString("content") ?: "",
                attachments = emptyList<Attachment>(), // см. ниже
                createdAt = getLong("createdAt") ?: 0,
                parentCommentId = getString("parentCommentId"),
                status = CommentStatus.valueOf(getString("status") ?: CommentStatus.PENDING.name)
            )
        } catch (e: Exception) { null }
    }

    private fun Comment.toMap() = mapOf(
        "discussionId" to discussionId,
        "authorId" to authorId,
        "content" to content,
        // attachments можно хранить списком строк-URL или hashMap'ом
        "createdAt" to createdAt,
        "parentCommentId" to parentCommentId,
        "status" to status.name
    )
}