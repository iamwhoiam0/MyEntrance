package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Discussion
import com.example.myentrance.domain.entities.DiscussionStatus
import com.example.myentrance.domain.repository.DiscussionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DiscussionRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DiscussionRepository {

    override suspend fun getDiscussions(buildingId: String): List<Discussion> {
        val snap = firestore.collection("discussions")
            .whereEqualTo("buildingId", buildingId)
            .get()
            .await()
        return snap.documents.mapNotNull { doc -> doc.toDiscussion() }
    }

    override suspend fun getDiscussionById(discussionId: String): Discussion? {
        val doc = firestore.collection("discussions").document(discussionId).get().await()
        return doc.toDiscussion()
    }

    override suspend fun createDiscussion(discussion: Discussion): Boolean {
        firestore.collection("discussions")
            .document(discussion.id)
            .set(discussion.toMap())
            .await()
        return true
    }

    override suspend fun updateDiscussion(discussion: Discussion): Boolean {
        firestore.collection("discussions")
            .document(discussion.id)
            .update(discussion.toMap())
            .await()
        return true
    }

    override suspend fun deleteDiscussion(discussionId: String): Boolean {
        firestore.collection("discussions").document(discussionId).delete().await()
        return true
    }

    override suspend fun searchDiscussions(buildingId: String, query: String): List<Discussion> {
        val snap = firestore.collection("discussions")
            .whereEqualTo("buildingId", buildingId)
            .whereGreaterThanOrEqualTo("title", query)
            .get().await()
        return snap.documents.mapNotNull { it.toDiscussion() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDiscussion(): Discussion? {
        return try {
            Discussion(
                id = id,
                title = getString("title") ?: "",
                content = getString("content") ?: "",
                authorId = getString("authorId") ?: "",
                buildingId = getString("buildingId") ?: "",
                category = getString("category") ?: "",
                isPinned = getBoolean("isPinned") ?: false,
                status = DiscussionStatus.valueOf(getString("status") ?: DiscussionStatus.PENDING.name),
                commentsCount = getLong("commentsCount")?.toInt() ?: 0,
                createdAt = getLong("createdAt") ?: 0,
                updatedAt = getLong("updatedAt") ?: 0
            )
        } catch (e: Exception) { null }
    }

    private fun Discussion.toMap() = mapOf(
        "title" to title,
        "content" to content,
        "authorId" to authorId,
        "buildingId" to buildingId,
        "category" to category,
        "isPinned" to isPinned,
        "status" to status.name,
        "commentsCount" to commentsCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}