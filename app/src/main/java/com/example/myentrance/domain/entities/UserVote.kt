package com.example.myentrance.domain.entities

data class UserVote(
    val id: String,
    val voteId: String,
    val userId: String,
    val optionId: String
)
