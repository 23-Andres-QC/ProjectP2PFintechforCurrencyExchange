package com.example.p2p.presentation.bank_accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateBankAccountRequest
import com.example.p2p.domain.repository.BankAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BankAccountsUiState(
    val isLoading: Boolean = false,
    val accounts: List<BankAccount> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class BankAccountsViewModel(
    private val repository: BankAccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankAccountsUiState())
    val uiState: StateFlow<BankAccountsUiState> = _uiState.asStateFlow()

    fun loadBankAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            when (val result = repository.listAccounts()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, accounts = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun addBankAccount(bankName: String, accountNumber: String, accountHolder: String, currency: String = "PEN") {
        if (bankName.isBlank() || accountNumber.isBlank() || accountHolder.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Todos los campos son obligatorios")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val request = CreateBankAccountRequest(
                bank_name = bankName,
                account_number = accountNumber,
                account_holder = accountHolder,
                currency = currency,
                is_primary = _uiState.value.accounts.isEmpty()
            )
            when (val result = repository.createAccount(request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Cuenta agregada con éxito")
                    loadBankAccounts()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteBankAccount(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            when (val result = repository.deleteAccount(id)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Cuenta eliminada con éxito")
                    loadBankAccounts()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    class Factory(private val repo: BankAccountRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BankAccountsViewModel(repo) as T
    }
}
