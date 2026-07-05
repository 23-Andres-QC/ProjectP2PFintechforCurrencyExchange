package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.BankAccountsApi
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateBankAccountRequest
import com.example.p2p.domain.repository.BankAccountRepository
import org.json.JSONObject

class BankAccountRepositoryImpl(
    private val api: BankAccountsApi
) : BankAccountRepository {

    override suspend fun listAccounts(): NetworkResult<List<BankAccount>> {
        return try {
            val response = api.listAccounts()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body.bank_accounts)
            } else {
                NetworkResult.Error(response.code(), parseBackendError(response.errorBody()?.string(), response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun createAccount(request: CreateBankAccountRequest): NetworkResult<BankAccount> {
        return try {
            val response = api.createAccount(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(response.code(), parseBackendError(response.errorBody()?.string(), response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun deleteAccount(id: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteAccount(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), parseBackendError(response.errorBody()?.string(), response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun setDefault(id: String): NetworkResult<Unit> {
        return try {
            val response = api.setDefault(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), parseBackendError(response.errorBody()?.string(), response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    private fun parseBackendError(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JSONObject(errorBody)
                val error = json.opt("error")
                when (error) {
                    is JSONObject -> {
                        val message = error.optString("message", "")
                        if (message.isNotBlank()) message else firstValidationMessage(error)
                    }
                    else -> {
                        val text = error?.toString().orEmpty()
                        text.ifBlank { httpMessage(code) }
                    }
                }
            } catch (e: Exception) {
                httpMessage(code)
            }
        }
        return httpMessage(code)
    }

    private fun firstValidationMessage(error: JSONObject): String {
        val keys = error.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = error.opt(key)
            val message = when {
                value is org.json.JSONArray && value.length() > 0 -> value.optString(0)
                value != null -> value.toString()
                else -> ""
            }
            if (message.isNotBlank()) return message
        }
        return ""
    }

    private fun httpMessage(code: Int) = when (code) {
        400 -> "Datos bancarios invalidos"
        401 -> "Sesion expirada, vuelve a iniciar sesion"
        403 -> "No tienes permiso para esta accion"
        404 -> "Cuenta bancaria no encontrada"
        409 -> "No se puede completar la accion con esta cuenta"
        else -> "Error $code"
    }
}
