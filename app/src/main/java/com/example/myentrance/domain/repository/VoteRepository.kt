package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.UserVote
import com.example.myentrance.domain.entities.Vote

interface VoteRepository {
    suspend fun getVotes(buildingId: String): List<Vote>
    suspend fun getVoteById(voteId: String): Vote?
    suspend fun createVote(vote: Vote): Boolean
    suspend fun submitUserVote(userVote: UserVote): Boolean
    suspend fun getVoteResults(voteId: String): Vote?
    suspend fun closeVote(voteId: String): Boolean
}
