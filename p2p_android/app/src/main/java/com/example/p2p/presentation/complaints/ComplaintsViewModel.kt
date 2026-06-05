package com.example.p2p.presentation.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Complaint
import com.example.p2p.data.remote.model.ComplaintType
import com.example.p2p.data.remote.model.CreateComplaintRequest
import com.example.p2p.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComplaintsUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitSuccess: Boolean = false,
    val complaints: List<Complaint> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

class ComplaintsViewModel(
    private val repository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplaintsUiState())
    val uiState: StateFlow<ComplaintsUiState> = _uiState.asStateFlow()

    init {
        loadMyComplaints()
    }

    fun loadMyComplaints(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getMyComplaints(page)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        complaints = result.data.complaints,
                        currentPage = result.data.pagination?.page ?: 1,
                        totalPages = result.data.pagination?.pages ?: 1
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

    fun createComplaint(
        type: ComplaintType,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, submitSuccess = false)
            val request = CreateComplaintRequest(
                    type = type.name.lowercase(),  // ← agrega .lowercase()
            description = description
            )
            when (val result = repository.createComplaint(request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                    loadMyComplaints()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message
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

    class Factory(private val repo: ComplaintRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ComplaintsViewModel(repo) as T
    }
}