package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Discussion

interface DiscussionRepository {
    suspend fun getDiscussions(buildingId: String): List<Discussion>
    suspend fun getDiscussionById(discussionId: String): Discussion?
    suspend fun createDiscussion(discussion: Discussion): Boolean
    suspend fun updateDiscussion(discussion: Discussion): Boolean
    suspend fun deleteDiscussion(discussionId: String): Boolean
    suspend fun searchDiscussions(buildingId: String, query: String): List<Discussion>
}
