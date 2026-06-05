package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.TransactionApi
import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.domain.repository.TransactionRepository
import org.json.JSONObject

class TransactionRepositoryImpl(
    private val api: TransactionApi
) : TransactionRepository {

    override suspend fun listTransactions(status: String?): NetworkResult<List<Transaction>> {
        return try {
            val response = api.listTransactions(status)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.transactions)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun getPendingTransactions(): NetworkResult<List<Transaction>> {
        return try {
            val response = api.pendingTransactions()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.transactions)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun getTransaction(id: String): NetworkResult<Transaction> {
        return try {
            val response = api.getTransaction(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun createTransaction(request: CreateTransactionRequest): NetworkResult<Transaction> {
        return try {
            val response = api.createTransaction(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = parseBackendError(response.errorBody()?.string(), response.code())
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }
    }

    private fun parseBackendError(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JSONObject(errorBody)
                // El backend retorna {"error": {"code": "...", "message": "..."}}
                val errorObj = json.optJSONObject("error")
                val errorCode = errorObj?.optString("code", "") ?: ""
                val message = errorObj?.optString("message", "") ?: ""
                when (errorCode) {
                    "OWN_OFFER" -> "No puedes comprar tu propia oferta"
                    "OFFER_UNAVAILABLE" -> "Esta oferta ya no está disponible"
                    "INVALID_AMOUNT" -> "Monto inválido: $message"
                    "MISSING_FIELD" -> "Datos incompletos"
                    else -> if (message.isNotBlank()) message else "Error $code"
                }
            } catch (e: Exception) {
                "Error $code"
            }
        }
        return when (code) {
            400 -> "Solicitud incorrecta"
            401 -> "Sesión expirada, vuelve a iniciar sesión"
            403 -> "No tienes permiso para esta acción"
            404 -> "No encontrado"
            else -> "Error $code"
        }
    }

    override suspend fun uploadVoucher(id: String, imageUrl: String): NetworkResult<Unit> {
        return try {
            val response = api.uploadVoucher(id, mapOf("image_url" to imageUrl))
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun uploadVoucherWithBase64(id: String, base64: String): NetworkResult<Unit> {
        return try {
            // Paso 1: subir imagen al servidor, obtener URL
            val uploadResponse = api.uploadImage(mapOf("image_base64" to base64))
            if (!uploadResponse.isSuccessful || uploadResponse.body() == null) {
                return NetworkResult.Error(uploadResponse.code(), "Error al subir la imagen")
            }
            val imageUrl = uploadResponse.body()!!["url"] ?: ""

            // Paso 2: registrar voucher con la URL obtenida
            val voucherResponse = api.uploadVoucher(id, mapOf("image_url" to imageUrl))
            if (voucherResponse.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(voucherResponse.code(), voucherResponse.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error al subir voucher")
        }
    }

    override suspend fun updateStatus(id: String, newStatus: String): NetworkResult<Transaction> {
        return try {
            val response = api.updateStatus(id, mapOf("status" to newStatus))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun confirmTransaction(id: String): NetworkResult<Unit> {
        return try {
            val response = api.confirmTransaction(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }
}
