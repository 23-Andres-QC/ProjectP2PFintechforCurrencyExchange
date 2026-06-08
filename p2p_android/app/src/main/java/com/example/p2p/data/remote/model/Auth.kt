package com.example.p2p.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val dni: String? = null,
    val phone: String? = null,
    val role: String = "buyer"
)

data class LoginResponse(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("kyc_verified") val kycVerified: Boolean = false,
    val rating: Float = 0f,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserResponse(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("kyc_verified") val kycVerified: Boolean = false,
    val rating: Float = 0f,
    @SerializedName("total_transactions") val totalTransactions: Int = 0,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true
)

data class ErrorBody(
    val error: ErrorDetail
)

data class ErrorDetail(
    val code: String,
    val message: String
)
