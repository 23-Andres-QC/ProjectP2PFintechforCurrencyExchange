package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse

interface DisputeRepository {

    suspend fun getMyDisputes(page: Int = 1, perPage: Int = 20): NetworkResult<DisputesResponse>

    suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute>

    suspend fun createDispute(
        transactionId: String,
        request: CreateDisputeRequest
    ): NetworkResult<Dispute>
}
