package com.example.p2p.presentation.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.User
import com.example.p2p.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KycSummaryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
)

class KycSummaryViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KycSummaryUiState())
    val uiState: StateFlow<KycSummaryUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.getMe()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, user = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            KycSummaryViewModel(userRepository) as T
    }
}
