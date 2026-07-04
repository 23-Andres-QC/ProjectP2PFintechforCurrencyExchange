package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.DisputeApi
import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse
import com.example.p2p.domain.repository.DisputeRepository
import org.json.JSONObject

class DisputeRepositoryImpl(
    private val api: DisputeApi
) : DisputeRepository {

    override suspend fun getMyDisputes(page: Int, perPage: Int): NetworkResult<DisputesResponse> =
        safeCall { api.getMyDisputes(page, perPage) }

    override suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute> =
        safeCall { api.getDisputeDetail(disputeId) }

    override suspend fun createDispute(
        transactionId: String,
        request: CreateDisputeRequest
    ): NetworkResult<Dispute> =
        safeCall { api.createDispute(transactionId, request) }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): NetworkResult<T> =
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMsg = parseBackendError(response.errorBody()?.string(), response.code())
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }

    private fun parseBackendError(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JSONObject(errorBody)
                val errorObj = json.optJSONObject("error")
                val errorCode = errorObj?.optString("code", "") ?: ""
                val message = errorObj?.optString("message", "") ?: ""
                when (errorCode) {
                    "CONFLICT" -> "Ya existe una disputa abierta para esta transaccion"
                    "INVALID_STATE" -> "Esta transaccion no permite abrir disputa"
                    "INVALID_REASON" -> "Selecciona un motivo valido"
                    "NOT_FOUND" -> "Transaccion no encontrada"
                    "FORBIDDEN" -> "No tienes permiso para disputar esta transaccion"
                    else -> if (message.isNotBlank()) message else httpMessage(code)
                }
            } catch (e: Exception) {
                httpMessage(code)
            }
        }
        return httpMessage(code)
    }

    private fun httpMessage(code: Int) = when (code) {
        400 -> "Datos invalidos"
        401 -> "Sesion expirada, vuelve a iniciar sesion"
        403 -> "No tienes permiso para esta accion"
        404 -> "Transaccion no encontrada"
        409 -> "Ya existe una disputa abierta para esta transaccion"
        else -> "Error $code"
    }
}
