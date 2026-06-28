package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.GroqRequest
import com.example.p2p.data.remote.model.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): GroqResponse
}
