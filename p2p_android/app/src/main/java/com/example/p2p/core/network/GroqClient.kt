package com.example.p2p.core.network

import com.example.p2p.data.remote.api.GroqApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GroqClient {
    val groqApi: GroqApi = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GroqApi::class.java)
}
