package com.example.p2p.presentation.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.data.remote.api.RatingApi
import com.example.p2p.data.remote.api.ReceivedRating
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val isLoading: Boolean = false,
    val ratings: List<ReceivedRating> = emptyList(),
    val average: Double = 0.0,
    val total: Int = 0,
    val distribution: Map<String, Int> = emptyMap(),
    val error: String? = null
)

class ReviewsViewModel(private val ratingApi: RatingApi) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ReviewsUiState(isLoading = true)
            try {
                val response = ratingApi.getReceivedRatings()
                val body = if (response.isSuccessful) response.body() else null
                if (body != null) {
                    _uiState.value = ReviewsUiState(
                        ratings = body.ratings,
                        average = body.average,
                        total = body.total,
                        distribution = body.distribution
                    )
                } else {
                    _uiState.value = ReviewsUiState(error = "Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ReviewsUiState(error = e.message)
            }
        }
    }

    class Factory(private val ratingApi: RatingApi) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReviewsViewModel(ratingApi) as T
    }
}
