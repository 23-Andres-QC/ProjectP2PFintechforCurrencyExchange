package com.example.p2p.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, fullName: String, dni: String = "") {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Todos los campos son requeridos")
            return
        }
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener mínimo 8 caracteres")
            return
        }
        val cleanDni = dni.trim().ifBlank { null }

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                // 1. Crear usuario en Firebase Auth
                firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()

                // 2. Guardar perfil en el backend (nombre, DNI, etc.)
                when (val result = authRepository.register(email.trim(), password, fullName.trim(), cleanDni)) {
                    is NetworkResult.Success -> {
                        _uiState.value = RegisterUiState(isSuccess = true)
                    }
                    is NetworkResult.Error -> {
                        // Si el backend falla, eliminar el usuario de Firebase para mantener consistencia
                        firebaseAuth.currentUser?.delete()?.await()
                        _uiState.value = RegisterUiState(error = result.message)
                    }
                    NetworkResult.Loading -> Unit
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("email-already-in-use") == true ->
                        "Este correo ya tiene una cuenta registrada"
                    e.message?.contains("weak-password") == true ->
                        "La contraseña es muy débil"
                    e.message?.contains("invalid-email") == true ->
                        "El correo electrónico no es válido"
                    e.message?.contains("network") == true ->
                        "Sin conexión a internet"
                    else -> e.message ?: "Error al crear la cuenta"
                }
                _uiState.value = RegisterUiState(error = msg)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(authRepository) as T
    }
}
