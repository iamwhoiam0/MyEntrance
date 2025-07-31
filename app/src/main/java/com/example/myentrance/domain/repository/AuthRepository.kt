package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.domain.entities.User

interface AuthRepository {

    suspend fun login(phone: String, otp: String, verificationId:String): AuthResult
    suspend fun register(name: String, phone: String, apartmentNumber: String, buildingId: String): AuthResult
    suspend fun logout()
    fun getCurrentUser(): User?
    suspend fun getUserByPhone(phone: String): User?
}