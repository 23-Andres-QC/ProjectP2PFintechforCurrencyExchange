package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.data.remote.model.NotificationsResponse

interface NotificationRepository {
    suspend fun getNotifications(): NetworkResult<NotificationsResponse>
    suspend fun getUnreadCount(): NetworkResult<Int>
    suspend fun markAllRead(): NetworkResult<Int>
    suspend fun markRead(id: String): NetworkResult<Notification>
    suspend fun deleteNotification(id: String): NetworkResult<Unit>
}
