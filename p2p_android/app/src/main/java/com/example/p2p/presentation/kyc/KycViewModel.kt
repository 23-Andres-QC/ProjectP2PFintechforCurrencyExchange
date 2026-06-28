package com.example.p2p.presentation.kyc

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.core.util.compressImageFromUri
import com.example.p2p.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun submitKyc(context: Context, signatureBytes: ByteArray? = null) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.dniFrontUri == null || currentState.dniBackUri == null || currentState.selfieUri == null) {
                _state.value = currentState.copy(error = "Todas las imágenes son obligatorias")
                return@launch
            }

            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val (dniFrontBytes, dniBackBytes, selfieBytes) = withContext(Dispatchers.IO) {
                    Triple(
                        compressImageFromUri(context, currentState.dniFrontUri),
                        compressImageFromUri(context, currentState.dniBackUri),
                        compressImageFromUri(context, currentState.selfieUri),
                    )
                }

                if (dniFrontBytes == null || dniBackBytes == null || selfieBytes == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Error al leer las imágenes")
                    return@launch
                }

                when (val result = userRepository.submitKyc(dniFrontBytes, dniBackBytes, selfieBytes, signatureBytes)) {
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

    /** Sube solo la firma del contrato, independiente de los documentos KYC (caso: usuario omitió KYC al registrarse). */
    fun uploadSignatureOnly(signatureBytes: ByteArray) {
        viewModelScope.launch {
            try {
                userRepository.uploadSignature(signatureBytes)
            } catch (_: Exception) {
                // Best-effort: no bloquea el flujo de registro si falla.
            }
        }
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KycViewModel(userRepository) as T
        }
    }
}
