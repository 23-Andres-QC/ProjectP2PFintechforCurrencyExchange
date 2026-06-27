package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.NotificationApi
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.data.remote.model.NotificationsResponse
import com.example.p2p.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val api: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(): NetworkResult<NotificationsResponse> = try {
        val r = api.getNotifications()
        val body = r.body()
        if (r.isSuccessful && body != null) NetworkResult.Success(body)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun getUnreadCount(): NetworkResult<Int> = try {
        val r = api.getUnreadCount()
        val body = r.body()
        if (r.isSuccessful && body != null) NetworkResult.Success(body.unread_count)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun markAllRead(): NetworkResult<Int> = try {
        val r = api.markAllRead()
        val body = r.body()
        if (r.isSuccessful && body != null) NetworkResult.Success(body.marked_read)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun markRead(id: String): NetworkResult<Notification> = try {
        val r = api.markRead(id)
        val body = r.body()
        if (r.isSuccessful && body != null) NetworkResult.Success(body)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun deleteNotification(id: String): NetworkResult<Unit> = try {
        val r = api.deleteNotification(id)
        if (r.isSuccessful) NetworkResult.Success(Unit)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }
}
