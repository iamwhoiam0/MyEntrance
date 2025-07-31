package com.example.myentrance.domain.entities

data class User(
    val id: String, // UID в Firebase
    val name: String,
    val phoneNumber: String,
    val role: Role,
    val isVerified: Boolean,
    val lastLoginAt: Long,
    val description: String,
    val apartmentNumber: String,
    val buildingId: String
)