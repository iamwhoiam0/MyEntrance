package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Role
import com.example.myentrance.domain.entities.User

interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun updateProfile(user: User): Boolean
    suspend fun searchUsers(query: String): List<User>
    suspend fun changeUserRole(userId: String, role: Role): Boolean
}
