package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.CreateRatingRequest
import com.example.p2p.data.remote.api.RatingApi
import com.example.p2p.data.remote.api.RatingResponse
import com.example.p2p.domain.repository.RatingRepository
import org.json.JSONObject

class RatingRepositoryImpl(
    private val api: RatingApi
) : RatingRepository {

    override suspend fun createRating(transactionId: String, score: Int, comment: String?): NetworkResult<RatingResponse> {
        return try {
            val response = api.createRating(CreateRatingRequest(transactionId, score, comment))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val msg = parseBackendError(response.errorBody()?.string(), response.code())
                NetworkResult.Error(response.code(), msg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    private fun parseBackendError(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JSONObject(errorBody)
                val errorObj = json.optJSONObject("error")
                val errorCode = errorObj?.optString("code", "") ?: ""
                val message = errorObj?.optString("message", "") ?: ""
                when (errorCode) {
                    "CONFLICT" -> "Ya calificaste esta transacción"
                    "INVALID_STATE" -> "Solo puedes calificar transacciones completadas"
                    "NOT_FOUND" -> "Transacción no encontrada"
                    else -> if (message.isNotBlank()) message else httpMessage(code)
                }
            } catch (e: Exception) {
                httpMessage(code)
            }
        }
        return httpMessage(code)
    }

    private fun httpMessage(code: Int) = when (code) {
        400 -> "Datos inválidos"
        401 -> "Sesión expirada"
        403 -> "No tienes permiso"
        404 -> "Transacción no encontrada"
        409 -> "Ya calificaste esta transacción"
        else -> "Error $code"
    }
}
