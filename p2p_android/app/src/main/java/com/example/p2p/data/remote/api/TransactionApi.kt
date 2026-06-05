package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.data.remote.model.TransactionsResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {
    @GET("transactions")
    suspend fun listTransactions(
        @Query("status") status: String? = null
    ): Response<TransactionsResponse>

    @GET("transactions/pending")
    suspend fun pendingTransactions(): Response<TransactionsResponse>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): Response<Transaction>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<Transaction>

    @POST("transactions/{id}/voucher")
    suspend fun uploadVoucher(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    @PATCH("transactions/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<Transaction>

    @POST("transactions/{id}/confirm")
    suspend fun confirmTransaction(
        @Path("id") id: String
    ): Response<Map<String, String>>

    @POST("uploads")
    suspend fun uploadImage(
        @Body body: Map<String, String>
    ): Response<Map<String, String>>
}
