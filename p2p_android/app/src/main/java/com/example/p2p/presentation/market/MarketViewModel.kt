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
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.domain.repository.BankAccountRepository
import com.example.p2p.domain.repository.NotificationRepository
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
    val bankAccounts: List<BankAccount> = emptyList(),
    val selectedBankAccountId: String? = null,
    val isLoadingAccounts: Boolean = false,
    val unreadCount: Int = 0,
    val activeTransactions: List<Transaction> = emptyList()
)

class MarketViewModel(
    private val offerRepository: OfferRepository,
    private val transactionRepository: TransactionRepository,
    private val bankAccountRepository: BankAccountRepository? = null,
    private val exchangeApi: ExchangeApi? = null,
    private val notificationRepository: NotificationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadExchangeRates()
        loadBankAccounts()
        loadUnreadCount()
        loadActiveTransactions()
    }

    fun loadUnreadCount() {
        if (notificationRepository == null) return
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is NetworkResult.Success ->
                    _uiState.value = _uiState.value.copy(unreadCount = result.data)
                else -> Unit
            }
        }
    }

    private fun loadBankAccounts() {
        if (bankAccountRepository == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAccounts = true)
            when (val result = bankAccountRepository.listAccounts()) {
                is NetworkResult.Success -> {
                    val accounts = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoadingAccounts = false,
                        bankAccounts = accounts,
                        selectedBankAccountId = accounts.firstOrNull()?.id
                    )
                }
                is NetworkResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoadingAccounts = false)
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun selectBankAccount(id: String) {
        _uiState.value = _uiState.value.copy(selectedBankAccountId = id)
    }

    private fun loadExchangeRates() {
        if (exchangeApi == null) return
        viewModelScope.launch {
            try {
                val usdResp = exchangeApi.getRates(from = "USD")
                val usdRates = if (usdResp.isSuccessful) usdResp.body()?.rates ?: emptyList() else emptyList()
                val eurResp = exchangeApi.getRates(from = "EUR")
                val eurRates = if (eurResp.isSuccessful) eurResp.body()?.rates ?: emptyList() else emptyList()
                val combined = (usdRates + eurRates).distinctBy { "${it.from_currency}_${it.to_currency}" }
                if (combined.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(exchangeRates = combined)
                }
            } catch (_: Exception) {}
        }
    }

    fun loadOffers(currency: String? = null, fiatCurrency: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = offerRepository.listOffers(currency, fiatCurrency)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, offers = result.data)
                is NetworkResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun createTransaction(request: CreateTransactionRequest, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = transactionRepository.createTransaction(request)) {
                is NetworkResult.Success -> { _uiState.value = _uiState.value.copy(isLoading = false); onSuccess(result.data.id) }
                is NetworkResult.Error   -> { _uiState.value = _uiState.value.copy(isLoading = false); onError(result.message) }
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun loadActiveTransactions() {
        viewModelScope.launch {
            when (val result = transactionRepository.listTransactions()) {
                is NetworkResult.Success -> {
                    val active = result.data.filter { it.status in listOf("pending", "accepted", "voucher_uploaded") }
                    _uiState.value = _uiState.value.copy(activeTransactions = active)
                }
                else -> Unit
            }
        }
    }

    fun matchOffer(currency: String, fiatCurrency: String, onMatched: (Offer) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val rates = _uiState.value.exchangeRates.associateBy { "${it.from_currency}_${it.to_currency}" }
            val marketRate = rates["${currency}_${fiatCurrency}"]?.rate
            val quickSaleOffer = if (marketRate != null) {
                _uiState.value.offers.filter { it.price_per_unit < marketRate }.minByOrNull { it.price_per_unit }
            } else null
            if (quickSaleOffer != null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onMatched(quickSaleOffer)
                return@launch
            }
            when (val result = offerRepository.matchOffer(currency, fiatCurrency)) {
                is NetworkResult.Success -> { _uiState.value = _uiState.value.copy(isLoading = false); onMatched(result.data) }
                is NetworkResult.Error   -> { _uiState.value = _uiState.value.copy(isLoading = false, error = result.message); onError(result.message) }
                NetworkResult.Loading    -> Unit
            }
        }
    }

    class Factory(
        private val offerRepository: OfferRepository,
        private val transactionRepository: TransactionRepository,
        private val bankAccountRepository: BankAccountRepository? = null,
        private val exchangeApi: ExchangeApi? = null,
        private val notificationRepository: NotificationRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MarketViewModel(offerRepository, transactionRepository, bankAccountRepository, exchangeApi, notificationRepository) as T
    }
}
