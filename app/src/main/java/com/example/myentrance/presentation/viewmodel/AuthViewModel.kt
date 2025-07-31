package com.example.myentrance.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myentrance.data.repository.AuthRepositoryImpl
import com.example.myentrance.domain.entities.AuthResult
import com.example.myentrance.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    // Возможно, добавить другие репозитории по мере необходимости
) : ViewModel() {

    // --- Внутренне состояние для процесса ---
    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _registrationData = MutableStateFlow<RegistrationData?>(null)
    val registrationData: StateFlow<RegistrationData?> = _registrationData

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState

    private val _progress = MutableStateFlow(false)
    val progress: StateFlow<Boolean> = _progress

    private val _takePictureEvent = MutableSharedFlow<Uri>()
    val takePictureEvent = _takePictureEvent.asSharedFlow()

    private var currentPhotoUri: Uri? = null

    // --- Служебные методы для управления состоянием ---

    fun setPhone(phone: String) { _phone.value = phone }
    fun setVerificationId(id: String) { _verificationId.value = id }
    fun resetOtp() { _verificationId.value = null }

    // --- 1. Проверка наличия пользователя перед отправкой OTP (LoginStep1) ---
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

    // --- 2. Хранение данных регистрации (RegisterStep1) ---
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

    // --- 3. Отправка одноразового кода (можно универсально для login/register) ---
    // Важно: фактический вызов PhoneAuthProvider делается в Fragment!
    // Здесь можно сделать только "запрос отправлен" для UI-информирования

    fun markOtpSent(verificationId: String) {
        setVerificationId(verificationId)
    }


    // --- 4. Логин по коду (LoginStep2) ---
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

    // --- 5. Регистрация нового пользователя (RegisterStep2): регистрация/создание профиля с OTP ---
    fun registerWithOtp(otp: String) {
        val reg = _registrationData.value
        val id = _verificationId.value
        if (reg == null || id == null) {
            _authState.value = AuthResult.Error("Недостаточно данных для регистрации")
            return
        }
        viewModelScope.launch {
            _progress.value = true
            val result = authRepository.login(reg.phone, otp, id)
            if (result is AuthResult.Success) {
                val regRes = authRepository.register(
                    phone = reg.phone,
                    name = reg.name,
                    apartmentNumber = reg.apartment,
                    buildingId = reg.building
                )
                _authState.value = regRes
            } else {
                _authState.value = result // ошибка входа по OTP
            }
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

/** Внутренняя модель для хранения черновика данных регистрации */
data class RegistrationData(
    val phone: String,
    val name: String,
    val apartment: String,
    val building: String
)



fun ProvideAuthRepository(): AuthRepository {
    return AuthRepositoryImpl(
        firebaseAuth = FirebaseAuth.getInstance(),
        firestore = FirebaseFirestore.getInstance(),
        storage = FirebaseStorage.getInstance()
    )
}
class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}