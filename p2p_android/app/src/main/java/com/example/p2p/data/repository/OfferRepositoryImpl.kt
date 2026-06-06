package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.OfferApi
import com.example.p2p.data.remote.model.CreateOfferRequest
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.domain.repository.OfferRepository
import org.json.JSONObject

class OfferRepositoryImpl(
    private val api: OfferApi
) : OfferRepository {

    override suspend fun listOffers(
        currency: String?,
        fiatCurrency: String?,
        offerType: String?
    ): NetworkResult<List<Offer>> {
        return try {
            val response = api.listOffers(currency, fiatCurrency, offerType)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.offers)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun createOffer(request: CreateOfferRequest): NetworkResult<Offer> {
        return try {
            val response = api.createOffer(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = parseBackendError(response.errorBody()?.string(), response.code())
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    private fun parseBackendError(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            return try {
                val json = JSONObject(errorBody)
                val msg = json.optJSONObject("error")?.optString("message", "") ?: ""
                when {
                    code == 403 && msg.contains("vendor", ignoreCase = true) ->
                        "Solo los vendedores pueden crear ofertas"
                    msg.isNotBlank() -> msg
                    else -> httpMessage(code)
                }
            } catch (e: Exception) {
                httpMessage(code)
            }
        }
        return httpMessage(code)
    }

    private fun httpMessage(code: Int) = when (code) {
        400 -> "Datos inválidos"
        401 -> "Sesión expirada, vuelve a iniciar sesión"
        403 -> "No tienes permiso para realizar esta acción"
        404 -> "No encontrado"
        else -> "Error $code"
    }

    override suspend fun getMyOffers(): NetworkResult<List<Offer>> {
        return try {
            val response = api.myOffers()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.offers)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun matchOffer(currency: String, fiatCurrency: String): NetworkResult<Offer> {
        return try {
            val response = api.matchOffer(mapOf("currency" to currency, "fiat_currency" to fiatCurrency))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun pauseOffer(offerId: String): NetworkResult<Offer> {
        return try {
            val response = api.updateOffer(offerId, mapOf("status" to "paused"))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun resumeOffer(offerId: String): NetworkResult<Offer> {
        return try {
            val response = api.updateOffer(offerId, mapOf("status" to "active"))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun deleteOffer(offerId: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteOffer(offerId)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                val errorMsg = parseBackendError(response.errorBody()?.string(), response.code())
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }
}
