package com.example.p2p.presentation.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.ExchangeApi
import com.example.p2p.data.remote.model.CreateOfferRequest
import com.example.p2p.domain.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PublishUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val marketRate: Double? = null,
    val isLoadingRate: Boolean = false
)

class PublishViewModel(
    private val offerRepository: OfferRepository,
    private val exchangeApi: ExchangeApi? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    fun loadExchangeRate(from: String, to: String) {
        if (exchangeApi == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRate = true, marketRate = null)
            try {
                val resp = exchangeApi.getRates(from = from)
                if (resp.isSuccessful) {
                    val rate = resp.body()?.rates?.find { it.to_currency == to }?.rate
                    _uiState.value = _uiState.value.copy(isLoadingRate = false, marketRate = rate)
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingRate = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingRate = false)
            }
        }
    }

    fun publishOffer(request: CreateOfferRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = false)
            when (val result = offerRepository.createOffer(request)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                is NetworkResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.value = PublishUiState()
    }

    class Factory(
        private val offerRepository: OfferRepository,
        private val exchangeApi: ExchangeApi? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PublishViewModel(offerRepository, exchangeApi) as T
    }
}
