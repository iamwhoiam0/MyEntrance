package com.example.myentrance.domain.entities

sealed class AuthResult {
    data class Success(val user: User, val token: String): AuthResult()
    data class Error(val message: String, val code: Int? = null): AuthResult()
    data object Loading : AuthResult()
}
