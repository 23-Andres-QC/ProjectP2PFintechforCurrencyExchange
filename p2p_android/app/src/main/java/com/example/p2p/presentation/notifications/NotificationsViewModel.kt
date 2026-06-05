package com.example.p2p.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
)

class NotificationsViewModel(
    private val repo: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadAndMarkRead()
    }

    fun loadAndMarkRead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repo.getNotifications()) {
                is NetworkResult.Success -> {
                    val data = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = data.notifications,
                        unreadCount = data.unread_count,
                    )
                    // Marcar todas como leídas automáticamente al abrir la pantalla
                    if (data.unread_count > 0) {
                        repo.markAllRead()
                        _uiState.value = _uiState.value.copy(unreadCount = 0)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repo.deleteNotification(id)
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.filter { it.id != id }
            )
        }
    }

    class Factory(private val repo: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationsViewModel(repo) as T
    }
}
