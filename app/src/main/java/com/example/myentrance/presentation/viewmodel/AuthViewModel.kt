package com.example.myentrance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.domain.entities.User
import com.example.myentrance.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    val currentUser: User?
        get() = authRepository.getCurrentUser()
    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _registrationData = MutableStateFlow<RegistrationData?>(null)
    val registrationData: StateFlow<RegistrationData?> = _registrationData

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState

    private val _progress = MutableStateFlow(false)
    val progress: StateFlow<Boolean> = _progress

    fun setPhone(phone: String) { _phone.value = phone }
    fun setVerificationId(id: String) { _verificationId.value = id }
    fun resetOtp() { _verificationId.value = null }

    fun checkUserExists(
        phone: String,
        onExists: () -> Unit,
        onNotExists: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _progress.value = true
            try {
                val user = authRepository.getUserByPhone(phone)
                _progress.value = false
                if (user != null) {
                    _phone.value = phone
                    onExists()
                } else {
                    onNotExists()
                }
            } catch (e: Exception) {
                _progress.value = false
                onError(e.message ?: "Ошибка проверки номера")
            }
        }
    }

    fun saveRegistrationDraft(
        phone: String,
        name: String,
        apartment: String,
        building: String
    ) {
        _registrationData.value =
            RegistrationData(phone, name, apartment, building)
        _phone.value = phone
    }

    fun markOtpSent(verificationId: String) {
        setVerificationId(verificationId)
    }

    fun loginWithOtp(otp: String) {
        val phone = _phone.value
        val id = _verificationId.value
        if (id == null) {
            _authState.value = AuthResult.Error("Код не был отправлен на этот номер")
            return
        }
        viewModelScope.launch {
            _progress.value = true
            val result = authRepository.login(phone, otp, id)
            _progress.value = false
            _authState.value = result
        }
    }

    fun checkUserNotExists(
        phone: String,
        onNotExists: () -> Unit,
        onExists: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserByPhone(phone)
                if (user == null) {
                    _phone.value = phone
                    onNotExists()
                } else {
                    onExists()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка проверки номера")
            }
        }
    }

    fun registerWithOtp(otp: String) {
        val reg = _registrationData.value
        val id = _verificationId.value
        if (reg == null || id == null) {
            _authState.value = AuthResult.Error("Недостаточно данных для регистрации")
            return
        }
        viewModelScope.launch {
            _progress.value = true

            val credential = PhoneAuthProvider.getCredential(id, otp)
            val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val firebaseUser = result.user
            if (firebaseUser == null) {
                _authState.value = AuthResult.Error("Ошибка входа: Пользователь не найден после подтверждения")
                _progress.value = false
                return@launch
            }

            val regRes = authRepository.register(
                phone = reg.phone,
                name = reg.name,
                apartmentNumber = reg.apartment,
                buildingId = reg.building
            )
            _authState.value = regRes
            _progress.value = false
        }
    }

    fun resetState() {
        _authState.value = null
        _registrationData.value = null
        _verificationId.value = null
        _progress.value = false
    }

}

data class RegistrationData(
    val phone: String,
    val name: String,
    val apartment: String,
    val building: String
)