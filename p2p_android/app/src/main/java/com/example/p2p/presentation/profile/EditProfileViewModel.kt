package com.example.p2p.presentation.profile

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

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EditProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = userRepository.getMe()) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, user = result.data)
                is NetworkResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun saveProfile(fullName: String, phone: String?) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            when (val result = userRepository.updateProfile(fullName, phone)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isSaving = false, user = result.data, saveSuccess = true)
                is NetworkResult.Error   -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val repo: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditProfileViewModel(repo) as T
    }
}
