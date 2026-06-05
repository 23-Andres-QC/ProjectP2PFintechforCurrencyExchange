package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.ExchangeRatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeApi {
    @GET("exchange/rates")
    suspend fun getRates(
        @Query("from") from: String = "USD"
    ): Response<ExchangeRatesResponse>
}
