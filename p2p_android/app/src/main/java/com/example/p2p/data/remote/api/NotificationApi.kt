package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.MarkedReadResponse
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.data.remote.model.NotificationsResponse
import com.example.p2p.data.remote.model.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @POST("notifications/mark-all-read")
    suspend fun markAllRead(): Response<MarkedReadResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): Response<Notification>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>
}
