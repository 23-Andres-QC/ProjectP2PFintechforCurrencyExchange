package com.example.p2p.data.remote.model

data class Notification(
    val id: String,
    val user_id: String,
    val type: String,       // login | transaction | voucher | dispute | offer | security | admin
    val title: String,
    val body: String,
    val is_read: Boolean,
    val resource_id: String?,
    val created_at: String,
    val updated_at: String,
)

data class NotificationsResponse(
    val notifications: List<Notification>,
    val unread_count: Int,
)

data class UnreadCountResponse(
    val unread_count: Int,
)

data class MarkedReadResponse(
    val marked_read: Int,
)
