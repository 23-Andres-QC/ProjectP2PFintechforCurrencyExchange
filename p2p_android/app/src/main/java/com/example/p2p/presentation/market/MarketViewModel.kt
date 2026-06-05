package com.example.p2p.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.ExchangeApi
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.ExchangeRate
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.domain.repository.BankAccountRepository
import com.example.p2p.domain.repository.OfferRepository
import com.example.p2p.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarketUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val offers: List<Offer> = emptyList(),
    val exchangeRates: List<ExchangeRate> = emptyList(),
    // Filtros de moneda
    val fromCurrency: String = "PEN",
    val toCurrency: String = "USD",
    // Cuentas bancarias del comprador
    val bankAccounts: List<BankAccount> = emptyList(),
    val selectedBankAccountId: String? = null,
    val isLoadingAccounts: Boolean = false
)

val AVAILABLE_CURRENCIES = listOf("PEN", "USD", "EUR", "BRL")

class MarketViewModel(
    private val offerRepository: OfferRepository,
    private val transactionRepository: TransactionRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val exchangeApi: ExchangeApi? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadOffers()
        loadExchangeRates()
        loadBankAccounts()
    }

    private fun loadExchangeRates() {
        if (exchangeApi == null) return
        viewModelScope.launch {
            try {
                val response = exchangeApi.getRates()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(exchangeRates = response.body()?.rates ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    fun loadOffers(currency: String? = null, fiatCurrency: String? = null) {
        val curr = currency ?: _uiState.value.toCurrency
        val fiat = fiatCurrency ?: _uiState.value.fromCurrency
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = offerRepository.listOffers(currency = curr, fiatCurrency = fiat)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, offers = result.data)
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun setFilter(from: String, to: String) {
        _uiState.value = _uiState.value.copy(fromCurrency = from, toCurrency = to)
        loadOffers(currency = to, fiatCurrency = from)
    }

    fun loadBankAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAccounts = true)
            when (val result = bankAccountRepository.listAccounts()) {
                is NetworkResult.Success -> {
                    val accounts = result.data
                    val primary = accounts.firstOrNull { it.is_primary } ?: accounts.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoadingAccounts = false,
                        bankAccounts = accounts,
                        selectedBankAccountId = primary?.id
                    )
                }
                else -> _uiState.value = _uiState.value.copy(isLoadingAccounts = false)
            }
        }
    }

    fun selectBankAccount(id: String) {
        _uiState.value = _uiState.value.copy(selectedBankAccountId = id)
    }

    fun createTransaction(
        request: CreateTransactionRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = transactionRepository.createTransaction(request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(result.data.id)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun matchOffer(
        currency: String,
        fiatCurrency: String,
        onMatched: (Offer) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = offerRepository.matchOffer(currency, fiatCurrency)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onMatched(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                    onError(result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    class Factory(
        private val offerRepository: OfferRepository,
        private val transactionRepository: TransactionRepository,
        private val bankAccountRepository: BankAccountRepository,
        private val exchangeApi: ExchangeApi? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MarketViewModel(offerRepository, transactionRepository, bankAccountRepository, exchangeApi) as T
    }
}
