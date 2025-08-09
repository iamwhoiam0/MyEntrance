package com.example.myentrance.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myentrance.domain.entities.Result
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.example.myentrance.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _updateResult = MutableSharedFlow<Result<Unit>>()
    val updateResult = _updateResult.asSharedFlow()

    private val _uploadAvatarResult = MutableSharedFlow<Result<String>>()
    val uploadAvatarResult = _uploadAvatarResult.asSharedFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _user.value = user
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _logoutEvent.emit(Unit)
            } catch (e: Exception) {
                _logoutEvent.emit(Unit)
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            val result = profileRepository.updateUser(updatedUser)
            _updateResult.emit(result)
            if (result is Result.Success) {
                _user.value = updatedUser
                authRepository.saveCurrentUser(updatedUser)
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            val result = profileRepository.uploadAvatar(uri, userId)
            _uploadAvatarResult.emit(result)

            if (result is Result.Success) {
                val currentUser = _user.value ?: return@launch
                val updatedUser = currentUser.copy(avatarUrl = result.data)
                _user.value = updatedUser
                authRepository.saveCurrentUser(updatedUser)
                updateUser(updatedUser)
            }
        }
    }

}

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(profileRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}