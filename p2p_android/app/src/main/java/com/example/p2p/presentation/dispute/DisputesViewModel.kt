package com.example.p2p.presentation.dispute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputeReason
import com.example.p2p.domain.repository.DisputeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

data class DisputesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val disputes: List<Dispute> = emptyList(),
    val totalPages: Int = 1,
    val currentPage: Int = 1,
    // Para el detalle de una disputa
    val selectedDispute: Dispute? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class DisputesViewModel(
    private val disputeRepository: DisputeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisputesUiState())
    val uiState: StateFlow<DisputesUiState> = _uiState.asStateFlow()

    init {
        loadMyDisputes()
    }

    // ── Carga disputas del usuario ────────────────────────────────────────────

    fun loadMyDisputes(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = disputeRepository.getMyDisputes(page)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        disputes = result.data.disputes,
                        currentPage = result.data.pagination?.page ?: 1,
                        totalPages = result.data.pagination?.pages ?: 1,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    // ── Detalle de una disputa ────────────────────────────────────────────────

    fun loadDisputeDetail(disputeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedDispute = null)

            when (val result = disputeRepository.getDisputeDetail(disputeId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedDispute = result.data,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    // ── Abrir disputa ─────────────────────────────────────────────────────────

    fun createDispute(
        transactionId: String,
        reason: String,
        description: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitSuccess = false)

            val request = CreateDisputeRequest(
                reason = reason,
                description = description?.takeIf { it.isNotBlank() }
            )

            when (val result = disputeRepository.createDispute(transactionId, request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                    )
                    loadMyDisputes()   // Refrescar lista
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message,
                    )
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val repo: DisputeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DisputesViewModel(repo) as T
    }
}
