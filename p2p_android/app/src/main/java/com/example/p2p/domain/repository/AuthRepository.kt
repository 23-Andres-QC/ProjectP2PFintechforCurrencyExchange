package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.LoginResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<LoginResponse>
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        dni: String? = null,
        termsAccepted: Boolean = false,
        termsUrl: String? = null,
        termsVersion: String? = null
    ): NetworkResult<LoginResponse>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}
