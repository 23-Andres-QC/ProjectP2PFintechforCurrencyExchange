package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse

interface DisputeRepository {
    /** Disputas donde el usuario es parte (buyer o vendor). */
    suspend fun getMyDisputes(page: Int = 1, perPage: Int = 20): NetworkResult<DisputesResponse>

    /** Detalle de una disputa específica. */
    suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute>

    /** Abrir disputa sobre una transacción. */
    suspend fun createDispute(
        transactionId: String,
        request: CreateDisputeRequest
    ): NetworkResult<Dispute>
}
