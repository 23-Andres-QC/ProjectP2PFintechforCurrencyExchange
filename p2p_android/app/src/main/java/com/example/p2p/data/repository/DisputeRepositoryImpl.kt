package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.DisputeApi
import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse
import com.example.p2p.domain.repository.DisputeRepository

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): NetworkResult<T> =
        try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }
}
