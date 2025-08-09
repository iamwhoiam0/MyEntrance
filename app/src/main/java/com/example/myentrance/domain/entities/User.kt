package com.example.myentrance.domain.entities

data class User(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val role: Role = Role.RESIDENT,
    val isVerified: Boolean = false,
    val lastLoginAt: Long = 0L,
    val description: String = "",
    val apartmentNumber: String = "",
    val buildingId: String = "",
    val avatarUrl: String? = null
)