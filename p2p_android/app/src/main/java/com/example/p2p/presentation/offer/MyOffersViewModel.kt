package com.example.p2p.presentation.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.domain.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OfferFilter { ALL, ACTIVE, PAUSED }

data class MyOffersUiState(
    val isLoading: Boolean = false,
    val offers: List<Offer> = emptyList(),
    val filteredOffers: List<Offer> = emptyList(),
    val activeFilter: OfferFilter = OfferFilter.ALL,
    val error: String? = null
)

class MyOffersViewModel(
    private val offerRepository: OfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyOffersUiState())
    val uiState: StateFlow<MyOffersUiState> = _uiState.asStateFlow()

    fun loadMyOffers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = offerRepository.getMyOffers()) {
                is NetworkResult.Success -> {
                    val offers = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        offers = offers,
                        filteredOffers = applyFilter(offers, _uiState.value.activeFilter)
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun setFilter(filter: OfferFilter) {
        _uiState.value = _uiState.value.copy(
            activeFilter = filter,
            filteredOffers = applyFilter(_uiState.value.offers, filter)
        )
    }

    private fun applyFilter(offers: List<Offer>, filter: OfferFilter): List<Offer> = when (filter) {
        OfferFilter.ALL    -> offers
        OfferFilter.ACTIVE -> offers.filter { it.status == "active" }
        OfferFilter.PAUSED -> offers.filter { it.status == "paused" }
    }

    fun pauseOffer(offerId: String) {
        viewModelScope.launch {
            when (offerRepository.pauseOffer(offerId)) {
                is NetworkResult.Success -> loadMyOffers()
                else -> Unit
            }
        }
    }

    fun resumeOffer(offerId: String) {
        viewModelScope.launch {
            when (offerRepository.resumeOffer(offerId)) {
                is NetworkResult.Success -> loadMyOffers()
                else -> Unit
            }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            when (offerRepository.deleteOffer(offerId)) {
                is NetworkResult.Success -> loadMyOffers()
                else -> Unit
            }
        }
    }

    class Factory(private val offerRepository: OfferRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MyOffersViewModel(offerRepository) as T
    }
}