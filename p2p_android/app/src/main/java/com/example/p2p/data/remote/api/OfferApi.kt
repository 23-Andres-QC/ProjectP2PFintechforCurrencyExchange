package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.CreateOfferRequest
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.data.remote.model.OffersResponse
import retrofit2.Response
import retrofit2.http.*

interface OfferApi {
    @GET("offers")
    suspend fun listOffers(
        @Query("currency") currency: String? = null,
        @Query("fiat_currency") fiatCurrency: String? = null,
        @Query("type") offerType: String? = null
    ): Response<OffersResponse>

    @POST("offers")
    suspend fun createOffer(@Body request: CreateOfferRequest): Response<Offer>

    @GET("offers/my-offers")
    suspend fun myOffers(): Response<OffersResponse>

    @POST("offers/match")
    suspend fun matchOffer(@Body body: Map<String, String>): Response<Offer>

    @PATCH("offers/{id}")
    suspend fun updateOffer(
        @Path("id") offerId: String,
        @Body body: Map<String, String>
    ): Response<Offer>

    @DELETE("offers/{id}")
    suspend fun deleteOffer(@Path("id") offerId: String): Response<Map<String, String>>
}
