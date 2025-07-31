package com.example.myentrance.domain.entities

data class Vote(
    val id: String,
    val title: String,
    val description: String,
    val options: List<VoteOption>,
    val buildingId: String,
    val creatorId: String,
    val startDate: Long,
    val endDate: Long,
    val isAnonymous: Boolean,
    val status: VoteStatus,
    val participantsCount: Int
)