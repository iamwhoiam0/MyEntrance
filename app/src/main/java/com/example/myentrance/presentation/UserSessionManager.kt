package com.example.myentrance.presentation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myentrance.domain.entities.Role
import com.example.myentrance.domain.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// extension property для DataStore
val Context.dataStore by preferencesDataStore("user_prefs")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // загрузка сессии из локального хранилища (dataStore) - почитать о корректности размером сохраняемых данных (лимит)
    suspend fun loadSession() { // После добавления полей считывать их тут и присваивать текущему пользователю.
        val prefs = context.dataStore.data.first()
        val userId = prefs[KEY_USER_ID]
        val userName = prefs[KEY_USER_NAME]
        _currentUser.value = if (userId != null) {
            User(
                id = userId,
                name = userName ?: "",
                phoneNumber = "",
                role = Role.RESIDENT,
                isVerified = false,
                lastLoginAt = 0L,
                description = "",
                apartmentNumber = "",
                buildingId = ""
            )
        } else null
    }

    suspend fun saveUser(user: User) { // Нужно добавить сохранение других важных полей User
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
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
