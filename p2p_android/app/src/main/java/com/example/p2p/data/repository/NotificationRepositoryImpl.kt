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
        if (r.isSuccessful && r.body() != null) NetworkResult.Success(r.body()!!)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun getUnreadCount(): NetworkResult<Int> = try {
        val r = api.getUnreadCount()
        if (r.isSuccessful && r.body() != null) NetworkResult.Success(r.body()!!.unread_count)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun markAllRead(): NetworkResult<Int> = try {
        val r = api.markAllRead()
        if (r.isSuccessful && r.body() != null) NetworkResult.Success(r.body()!!.marked_read)
        else NetworkResult.Error(r.code(), r.message())
    } catch (e: Exception) {
        NetworkResult.Error(-1, e.message ?: "Error")
    }

    override suspend fun markRead(id: String): NetworkResult<Notification> = try {
        val r = api.markRead(id)
        if (r.isSuccessful && r.body() != null) NetworkResult.Success(r.body()!!)
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
