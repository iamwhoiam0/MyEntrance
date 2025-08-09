package com.example.myentrance.data.repository

import android.util.Log
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.domain.entities.Role
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.presentation.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userSessionManager: UserSessionManager
) : AuthRepository {

    override suspend fun login(phone: String, otp: String, verificationId: String): AuthResult {
        return try {
            if (verificationId.isBlank()) return AuthResult.Error("Не получен verificationId")
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Пользователь не найден")

            val user = fetchDomainUser(firebaseUser)
            if (user != null) {
                userSessionManager.saveUser(user)
                AuthResult.Success(user, token = firebaseUser.getIdToken(false).await().token ?: "")
            } else {
                AuthResult.Error("Профиль пользователя не найден")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Ошибка входа")
        }
    }

    override suspend fun register(
        name: String,
        phone: String,
        apartmentNumber: String,
        buildingId: String
    ): AuthResult {
        try {
            val currentUser = firebaseAuth.currentUser ?: return AuthResult.Error("Сначала подтвердите номер через SMS")
            val userDoc = hashMapOf(
                "name" to name,
                "phoneNumber" to phone,
                "role" to Role.RESIDENT.name,
                "isVerified" to false,
                "lastLoginAt" to System.currentTimeMillis(),
                "description" to "",
                "apartmentNumber" to apartmentNumber,
                "buildingId" to buildingId,
                "avatarUrl" to null
            )

            firestore.collection("users")
                .document(currentUser.uid)
                .set(userDoc)
                .await()

            val user = fetchDomainUser(currentUser)
            if (user != null) {
                userSessionManager.saveUser(user)
                return AuthResult.Success(user, token = currentUser.getIdToken(false).await().token ?: "")
            } else {
                return AuthResult.Error("Ошибка при получении профиля после регистрации")
            }
        } catch (e: Exception) {
            return AuthResult.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun logout() {
        try {
            firebaseAuth.signOut()

            userSessionManager.clearSession()

            Log.d("AuthRepository", "Пользователь успешно вышел")

        } catch (e: Exception) {
            Log.e("AuthRepository", "ошибка при выходе", e)
            throw e
        }
    }

    override fun getCurrentUser(): User? {
        return userSessionManager.currentUser.value
    }

    override suspend fun getUserByPhone(phone: String): User? {
        val query = firestore.collection("users")
            .whereEqualTo("phoneNumber", phone)
            .limit(1)
            .get()
            .await()

        val doc = query.documents.firstOrNull() ?: return null

        return User(
            id = doc.id,
            name = doc.getString("name") ?: "",
            phoneNumber = doc.getString("phoneNumber") ?: "",
            role = Role.valueOf(doc.getString("role") ?: Role.RESIDENT.name),
            isVerified = doc.getBoolean("isVerified") ?: false,
            lastLoginAt = doc.getLong("lastLoginAt") ?: 0,
            description = doc.getString("description") ?: "",
            apartmentNumber = doc.getString("apartmentNumber") ?: "",
            buildingId = doc.getString("buildingId") ?: "",
            avatarUrl = doc.getString("avatarUrl"),
        )
    }

    private suspend fun fetchDomainUser(firebaseUser: FirebaseUser): User? {
        val doc = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .await()

        return if (doc.exists()) {
            User(
                id = firebaseUser.uid,
                name = doc.getString("name") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                role = Role.valueOf(doc.getString("role") ?: Role.RESIDENT.name),
                isVerified = doc.getBoolean("isVerified") ?: false,
                lastLoginAt = doc.getLong("lastLoginAt") ?: 0,
                description = doc.getString("description") ?: "",
                apartmentNumber = doc.getString("apartmentNumber") ?: "",
                buildingId = doc.getString("buildingId") ?: "",
                avatarUrl = doc.getString("avatarUrl")
            )
        } else null
    }
    override suspend fun saveCurrentUser(user: User) {
        userSessionManager.saveUser(user)
    }
}