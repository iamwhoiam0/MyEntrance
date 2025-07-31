package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.UserVote
import com.example.myentrance.domain.entities.Vote
import com.example.myentrance.domain.entities.VoteOption
import com.example.myentrance.domain.entities.VoteStatus
import com.example.myentrance.domain.repository.VoteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VoteRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VoteRepository {

    override suspend fun getVotes(buildingId: String): List<Vote> {
        val snap = firestore.collection("votes")
            .whereEqualTo("buildingId", buildingId)
            .get().await()
        return snap.documents.mapNotNull { it.toVote() }
    }

    override suspend fun getVoteById(voteId: String): Vote? {
        val doc = firestore.collection("votes").document(voteId).get().await()
        return doc.toVote()
    }

    override suspend fun createVote(vote: Vote): Boolean {
        firestore.collection("votes")
            .document(vote.id)
            .set(vote.toMap())
            .await()
        return true
    }

    override suspend fun submitUserVote(userVote: UserVote): Boolean {
        firestore.collection("user_votes")
            .document(userVote.id)
            .set(userVote.toMap())
            .await()
        return true
    }

    override suspend fun getVoteResults(voteId: String): Vote? {
        // Можно дополнять подсчётом голосов
        return getVoteById(voteId)
    }

    override suspend fun closeVote(voteId: String): Boolean {
        firestore.collection("votes").document(voteId)
            .update("status", VoteStatus.CLOSED.name).await()
        return true
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toVote(): Vote? {
        return try {
            Vote(
                id = id,
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                options = (get("options") as? List<Map<String, Any>>)
                    ?.map { m -> VoteOption(m["id"].toString(), m["text"].toString()) } ?: emptyList(),
                buildingId = getString("buildingId") ?: "",
                creatorId = getString("creatorId") ?: "",
                startDate = getLong("startDate") ?: 0,
                endDate = getLong("endDate") ?: 0,
                isAnonymous = getBoolean("isAnonymous") ?: false,
                status = VoteStatus.valueOf(getString("status") ?: VoteStatus.ACTIVE.name),
                participantsCount = getLong("participantsCount")?.toInt() ?: 0
            )
        } catch (e: Exception) { null }
    }

    private fun Vote.toMap() = mapOf(
        "title" to title,
        "description" to description,
        "options" to options.map { mapOf("id" to it.id, "text" to it.text) },
        "buildingId" to buildingId,
        "creatorId" to creatorId,
        "startDate" to startDate,
        "endDate" to endDate,
        "isAnonymous" to isAnonymous,
        "status" to status.name,
        "participantsCount" to participantsCount
    )

    private fun UserVote.toMap() = mapOf(
        "voteId" to voteId,
        "userId" to userId,
        "optionId" to optionId
    )
}