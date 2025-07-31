package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Role
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
        val doc = firestore.collection("users").document(userId).get().await()
        return if (doc.exists()) {
            User(
                id = userId,
                phoneNumber = doc.getString("phoneNumber") ?: "",
                name = doc.getString("name") ?: "",
                role = Role.valueOf(doc.getString("role") ?: Role.RESIDENT.name),
                isVerified = doc.getBoolean("isVerified") ?: false,
                lastLoginAt = doc.getLong("lastLoginAt") ?: 0L,
                description = doc.getString("description") ?: "",
                apartmentNumber = doc.getString("apartmentNumber") ?: "",
                buildingId = doc.getString("buildingId") ?: ""
            )
        } else null
    }

    override suspend fun updateProfile(user: User): Boolean {
        val data = mapOf(
            "phoneNumber" to user.phoneNumber,
            "name" to user.name,
            "role" to user.role.name,
            "isVerified" to user.isVerified,
            "lastLoginAt" to user.lastLoginAt,
            "description" to user.description,
            "apartmentNumber" to user.apartmentNumber,
            "buildingId" to user.buildingId
        )
        firestore.collection("users").document(user.id).update(data).await()
        return true
    }

    override suspend fun searchUsers(query: String): List<User> {
        // Простой пример поиска по имени
        val snap = firestore.collection("users")
            .whereEqualTo("name", query)
            .get().await()
        return snap.documents.mapNotNull { doc ->
            getUserById(doc.id)
        }
    }

    override suspend fun changeUserRole(userId: String, role: Role): Boolean {
        firestore.collection("users")
            .document(userId)
            .update("role", role.name).await()
        return true
    }
}