package com.example.myentrance.presentation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myentrance.domain.entities.Role
import com.example.myentrance.domain.entities.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

// extension property для DataStore
val Context.dataStore by preferencesDataStore("user_prefs")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_VERIFIED = stringPreferencesKey("user_verified")
        private val KEY_USER_DESCRIPTION = stringPreferencesKey("user_description")
        private val KEY_USER_APARTMENT = stringPreferencesKey("user_apartment")
        private val KEY_USER_BUILDING = stringPreferencesKey("user_building")
        private val KEY_USER_AVATAR_URL = stringPreferencesKey("user_avatar_url")
        private val KEY_USER_LAST_LOGIN = stringPreferencesKey("user_last_login")
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun loadSession() {
        val prefs = context.dataStore.data.first()
        val userId = prefs[KEY_USER_ID]

        _currentUser.value = if (userId != null) {
            User(
                id = userId,
                name = prefs[KEY_USER_NAME] ?: "",
                phoneNumber = prefs[KEY_USER_PHONE] ?: "",
                role = Role.valueOf(prefs[KEY_USER_ROLE] ?: Role.RESIDENT.name),
                isVerified = prefs[KEY_USER_VERIFIED]?.toBoolean() ?: false,
                lastLoginAt = prefs[KEY_USER_LAST_LOGIN]?.toLong() ?: 0L,
                description = prefs[KEY_USER_DESCRIPTION] ?: "",
                apartmentNumber = prefs[KEY_USER_APARTMENT] ?: "",
                buildingId = prefs[KEY_USER_BUILDING] ?: "",
                avatarUrl = prefs[KEY_USER_AVATAR_URL]
            )
        } else null
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_PHONE] = user.phoneNumber
            prefs[KEY_USER_ROLE] = user.role.name
            prefs[KEY_USER_VERIFIED] = user.isVerified.toString()
            prefs[KEY_USER_DESCRIPTION] = user.description
            prefs[KEY_USER_APARTMENT] = user.apartmentNumber
            prefs[KEY_USER_BUILDING] = user.buildingId
            prefs[KEY_USER_LAST_LOGIN] = user.lastLoginAt.toString()
            user.avatarUrl?.let { prefs[KEY_USER_AVATAR_URL] = it }
        }
        _currentUser.value = user
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        _currentUser.value = null
    }
}
