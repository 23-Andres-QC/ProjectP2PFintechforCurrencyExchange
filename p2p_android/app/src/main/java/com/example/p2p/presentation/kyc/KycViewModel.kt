package com.example.p2p.presentation.kyc

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KycState(
    val currentStep: Int = 1,
    val dniFrontUri: Uri? = null,
    val dniBackUri: Uri? = null,
    val selfieUri: Uri? = null,
    val isScanning: Boolean = false,
    val scanningProgress: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class KycViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(KycState())
    val state: StateFlow<KycState> = _state.asStateFlow()

    fun onDniFrontSelected(uri: Uri) {
        _state.value = _state.value.copy(dniFrontUri = uri)
    }

    fun onDniBackSelected(uri: Uri) {
        _state.value = _state.value.copy(dniBackUri = uri)
    }

    fun onSelfieSelected(uri: Uri) {
        _state.value = _state.value.copy(selfieUri = uri)
    }

    fun nextStep() {
        val next = _state.value.currentStep + 1
        if (next <= 3) {
            _state.value = _state.value.copy(currentStep = next)
        }
    }

    fun startScanning() {
        _state.value = _state.value.copy(isScanning = true)
        viewModelScope.launch {
            var progress = 0f
            while (progress < 1f) {
                kotlinx.coroutines.delay(50)
                progress += 0.02f
                _state.value = _state.value.copy(scanningProgress = progress)
            }
        }
    }

    fun submitKyc(context: Context) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.dniFrontUri == null || currentState.dniBackUri == null || currentState.selfieUri == null) {
                _state.value = currentState.copy(error = "Todas las imágenes son obligatorias")
                return@launch
            }

            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val dniFrontBytes = context.contentResolver.openInputStream(currentState.dniFrontUri)?.readBytes()
                val dniBackBytes = context.contentResolver.openInputStream(currentState.dniBackUri)?.readBytes()
                val selfieBytes = context.contentResolver.openInputStream(currentState.selfieUri)?.readBytes()

                if (dniFrontBytes == null || dniBackBytes == null || selfieBytes == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Error al leer las imágenes")
                    return@launch
                }

                when (val result = userRepository.submitKyc(dniFrontBytes, dniBackBytes, selfieBytes)) {
                    is NetworkResult.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    }
                    is NetworkResult.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KycViewModel(userRepository) as T
        }
    }
}
