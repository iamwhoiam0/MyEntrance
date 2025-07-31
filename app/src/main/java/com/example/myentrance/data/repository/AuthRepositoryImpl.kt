package com.example.myentrance.data.repository

import android.util.Log
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.domain.entities.Role
import com.google.firebase.auth.FirebaseAuth
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : AuthRepository {


    override suspend fun login(phone: String, otp: String, verificationId: String): AuthResult {
        return try {
            if (verificationId.isBlank()) return AuthResult.Error("Не получен verificationId")
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            val user = firebaseUser?.let { fetchDomainUser(it) }
            if (user != null)
                AuthResult.Success(user, token = firebaseUser.getIdToken(false).await().token ?: "")
            else
                AuthResult.Error("Профиль пользователя не найден")
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
        return try {
            // Получение sms-credential и регистрация пользователя — происходит из UI слоя через callback
            // Здесь предполагается, что пользователь уже авторизован через Firebase по номеру

            val currentUser = firebaseAuth.currentUser
                ?: return AuthResult.Error("Сначала подтвердите номер по SMS")

            Log.d("AuthRepositoryImpl", "CurrentUser UID: ${currentUser.uid}")

            // Формируем профиль пользователя в Firestore
            val userDoc = hashMapOf(
                "name" to name,
                "phoneNumber" to phone,
                "role" to Role.RESIDENT.name,
                "isVerified" to false,
                "lastLoginAt" to System.currentTimeMillis(),
                "description" to "",
                "apartmentNumber" to apartmentNumber,
                "buildingId" to buildingId
            )
            firestore.collection("users")
                .document(currentUser.uid)
                .set(userDoc)
                .await()

            delay(500)
            return try {
                val user = fetchDomainUser(currentUser) // повторное чтение профиля
                AuthResult.Success(user, token = currentUser.getIdToken(false).await().token ?: "")
            } catch (e: Exception) {
                AuthResult.Error("Ошибка получения профиля после регистрации: ${e.message}")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Ошибка регистрации")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            // Здесь только ID, имя и телефон, остальное — через Firestore (асинхронно)
            User(
                id = it.uid,
                name = it.displayName ?: "",
                phoneNumber = it.phoneNumber ?: "",
                role = Role.RESIDENT,
                isVerified = false,
                lastLoginAt = it.metadata?.lastSignInTimestamp ?: 0,
                description = "",
                apartmentNumber = "",
                buildingId = "",
            )
        }
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
            buildingId = doc.getString("buildingId") ?: ""
        )
    }

    private suspend fun fetchDomainUser(firebaseUser: FirebaseUser): User {
        val doc = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .await()
        return User(
            id = firebaseUser.uid,
            name = doc.getString("name") ?: "",
            phoneNumber = doc.getString("phoneNumber") ?: "",
            role = Role.valueOf(doc.getString("role") ?: Role.RESIDENT.name),
            isVerified = doc.getBoolean("isVerified") ?: false,
            lastLoginAt = doc.getLong("lastLoginAt") ?: 0,
            description = doc.getString("description") ?: "",
            apartmentNumber = doc.getString("apartmentNumber") ?: "",
            buildingId = doc.getString("buildingId") ?: "",
        )
    }
}
