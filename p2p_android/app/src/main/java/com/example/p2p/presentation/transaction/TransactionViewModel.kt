package com.example.p2p.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val transaction: Transaction? = null
)

class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _pendingTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val pendingTransactions: StateFlow<List<Transaction>> = _pendingTransactions.asStateFlow()

    private val _buyerTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val buyerTransactions: StateFlow<List<Transaction>> = _buyerTransactions.asStateFlow()

    fun loadBuyerTransactions(currentUserId: String) {
        viewModelScope.launch {
            when (val result = transactionRepository.listTransactions()) {
                is NetworkResult.Success -> {
                    _buyerTransactions.value = result.data.filter { txn ->
                        txn.buyer_id == currentUserId &&
                            txn.status in listOf("pending", "accepted", "voucher_uploaded", "completed")
                    }
                }
                else -> Unit
            }
        }
    }

    fun loadPendingTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.getPendingTransactions()) {
                is NetworkResult.Success -> {
                    val activeTxns = result.data.filter { txn ->
                        txn.status in listOf("pending", "accepted", "voucher_uploaded")
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _pendingTransactions.value = activeTxns
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.getTransaction(id)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, transaction = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.updateStatus(id, status)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, transaction = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadVoucher(id: String, imageUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.uploadVoucher(id, imageUrl)) {
                is NetworkResult.Success -> loadTransaction(id)
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun uploadVoucherFromBase64(id: String, base64: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.uploadVoucherWithBase64(id, base64)) {
                is NetworkResult.Success -> loadTransaction(id)
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading -> Unit
            }
        }
    }
    fun uploadVendorVoucherFromBase64(
        id: String,
        base64: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.uploadVendorVoucherWithBase64(id, base64)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
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

    suspend fun uploadVoucherFromBase64Async(id: String, base64: String): Boolean {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        return when (val result = transactionRepository.uploadVoucherWithBase64(id, base64)) {
            is NetworkResult.Success -> { loadTransaction(id); true }
            is NetworkResult.Error -> { _uiState.value = _uiState.value.copy(isLoading = false, error = result.message); false }
            NetworkResult.Loading -> false
        }
    }

    fun confirmTransaction(id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.confirmTransaction(id)) {
                is NetworkResult.Success -> {
                    loadTransaction(id)
                    loadPendingTransactions()
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

    fun acceptTransaction(id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.updateStatus(id, "accepted")) {
                is NetworkResult.Success -> { loadPendingTransactions(); onSuccess() }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun cancelTransaction(id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = transactionRepository.updateStatus(id, "cancelled")) {
                is NetworkResult.Success -> { loadPendingTransactions(); onSuccess() }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    class Factory(private val repo: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TransactionViewModel(repo) as T
    }
}
