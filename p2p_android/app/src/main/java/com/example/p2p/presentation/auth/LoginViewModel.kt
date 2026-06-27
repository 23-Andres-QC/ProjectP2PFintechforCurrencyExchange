package com.example.p2p.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.ApiClient
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Email y contraseña son requeridos")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                // 1. Firebase Auth login
                firebaseAuth.signInWithEmailAndPassword(
                    state.email.trim(),
                    state.password
                ).await()

                // 2. Backend login para obtener JWT y datos del perfil
                when (val result = authRepository.login(state.email.trim(), state.password)) {
                    is NetworkResult.Success -> {
                        // 3. Registrar token FCM tras login exitoso
                        registerFcmToken()
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    }
                    NetworkResult.Loading -> Unit
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("no user record") == true ||
                    e.message?.contains("wrong-password") == true ||
                    e.message?.contains("invalid-credential") == true ->
                        "Email o contraseña incorrectos"
                    e.message?.contains("network") == true ->
                        "Sin conexión a internet"
                    else -> e.message ?: "Error al iniciar sesión"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
        }
    }

    private suspend fun registerFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            ApiClient.notificationApi.registerFcmToken(mapOf("fcm_token" to token))
        } catch (e: Exception) {
            Log.w("LoginViewModel", "No se pudo registrar el token FCM", e)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(authRepository) as T
    }
}
