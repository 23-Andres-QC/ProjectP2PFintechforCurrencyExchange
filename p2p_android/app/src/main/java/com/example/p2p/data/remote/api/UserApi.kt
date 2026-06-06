package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/me")
    suspend fun getMe(): Response<User>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body body: Map<String, String?>): Response<User>

    @Multipart
    @POST("users/kyc")
    suspend fun submitKyc(
        @Part dniFront: MultipartBody.Part,
        @Part dniBack: MultipartBody.Part,
        @Part selfie: MultipartBody.Part
    ): Response<Unit>
}
