package com.example.p2p.presentation.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.RatingResponse
import com.example.p2p.domain.repository.RatingRepository
import com.example.p2p.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RatingViewModel(
    private val repository: RatingRepository,
    private val transactionRepository: TransactionRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState: StateFlow<RatingUiState> = _uiState.asStateFlow()

    fun submitRating(transactionId: String, score: Int, comment: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
            when (val result = repository.createRating(transactionId, score, comment)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun closeTransaction(transactionId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            transactionRepository?.updateStatus(transactionId, "closed")
            onDone()
        }
    }

    fun resetState() {
        _uiState.value = RatingUiState()
    }

    class Factory(
        private val repo: RatingRepository,
        private val txnRepo: TransactionRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RatingViewModel(repo, txnRepo) as T
    }
}
