package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.CreateDisputeRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DisputeApi {

    /**
     * Disputas del usuario autenticado (como comprador o vendedor).
     * GET /api/v1/disputes/my-disputes
     */
    @GET("disputes/my-disputes")
    suspend fun getMyDisputes(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<DisputesResponse>

    /**
     * Detalle de una disputa por id.
     * GET /api/v1/disputes/{id}
     */
    @GET("disputes/{id}")
    suspend fun getDisputeDetail(
        @Path("id") disputeId: String
    ): Response<Dispute>

    /**
     * Abrir una nueva disputa sobre una transacción.
     * POST /api/v1/transactions/{id}/dispute
     */
    @POST("transactions/{id}/dispute")
    suspend fun createDispute(
        @Path("id") transactionId: String,
        @Body request: CreateDisputeRequest
    ): Response<Dispute>
}
