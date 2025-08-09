package com.example.myentrance.domain.repository

import android.net.Uri
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.entities.Result

interface ProfileRepository {
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun uploadAvatar(uri: Uri, userId: String): Result<String>
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Result<Unit>
}

