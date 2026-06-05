package com.example.p2p.data.remote.model

data class ExchangeRate(
    val id: String? = null,
    val from_currency: String,
    val to_currency: String,
    val rate: Double,
    val updated_at: String? = null
)

data class ExchangeRatesResponse(
    val rates: List<ExchangeRate>
)
