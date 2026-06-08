package com.example.p2p.data.repository

import com.example.p2p.core.network.ApiClient
import com.example.p2p.core.network.NetworkResult
import com.example.p2p.core.security.TokenManager
import com.example.p2p.data.remote.model.ErrorBody
import com.example.p2p.data.remote.model.LoginRequest
import com.example.p2p.data.remote.model.LoginResponse
import com.example.p2p.data.remote.model.RegisterRequest
import com.example.p2p.domain.repository.AuthRepository
import com.google.gson.Gson

class AuthRepositoryImpl(
    private val tokenManager: TokenManager
) : AuthRepository {

    private val api = ApiClient.authApi
    private val gson = Gson()

    override suspend fun login(email: String, password: String): NetworkResult<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    userId = body.id,
                    role = body.role,
                    name = body.fullName,
                    email = body.email
                )
                NetworkResult.Success(body)
            } else {
                val error = parseError(response.errorBody()?.string())
                NetworkResult.Error(response.code(), error)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Network error")
        }
    }

    override suspend fun register(email: String, password: String, fullName: String, dni: String?): NetworkResult<LoginResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password, fullName, dni = dni?.ifBlank { null }))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    userId = body.id,
                    role = body.role,
                    name = body.fullName,
                    email = body.email
                )
                NetworkResult.Success(body)
            } else {
                val error = parseError(response.errorBody()?.string())
                NetworkResult.Error(response.code(), error)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Network error")
        }
    }

    override suspend fun logout() {
        try { api.logout() } catch (_: Exception) {}
        tokenManager.clearSession()
    }

    override suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    private fun parseError(body: String?): String {
        if (body == null) return "Unknown error"
        return try {
            gson.fromJson(body, ErrorBody::class.java).error.message
        } catch (_: Exception) {
            body
        }
    }
}
