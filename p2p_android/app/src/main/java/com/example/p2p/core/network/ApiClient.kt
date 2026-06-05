package com.example.p2p.core.network

import android.content.Context
import com.example.p2p.core.security.TokenManager
import com.example.p2p.data.remote.api.AdminApi
import com.example.p2p.data.remote.api.AuthApi
import com.example.p2p.data.remote.api.BankAccountsApi
import com.example.p2p.data.remote.api.ComplaintApi
import com.example.p2p.data.remote.api.DisputeApi
import com.example.p2p.data.remote.api.ExchangeApi
import com.example.p2p.data.remote.api.NotificationApi
import com.example.p2p.data.remote.api.OfferApi
import com.example.p2p.data.remote.api.RatingApi
import com.example.p2p.data.remote.api.TransactionApi
import com.example.p2p.data.remote.api.UserApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Use 10.0.2.2 for Android emulator → host machine localhost
    private const val BASE_URL = "http://10.0.2.2:5000/api/v1/"

    private var tokenManager: TokenManager? = null

    fun init(context: Context) {
        tokenManager = TokenManager.getInstance(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { tokenManager?.getAccessToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val complaintApi: ComplaintApi by lazy { retrofit.create(ComplaintApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val offerApi: OfferApi by lazy { retrofit.create(OfferApi::class.java) }
    val transactionApi: TransactionApi by lazy { retrofit.create(TransactionApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val disputeApi: DisputeApi by lazy { retrofit.create(DisputeApi::class.java) }
    val adminApi: AdminApi by lazy { retrofit.create(AdminApi::class.java) }
    val ratingApi: RatingApi by lazy { retrofit.create(RatingApi::class.java) }
    val bankAccountsApi: BankAccountsApi by lazy { retrofit.create(BankAccountsApi::class.java) }
    val exchangeApi: ExchangeApi by lazy { retrofit.create(ExchangeApi::class.java) }
    val notificationApi: NotificationApi by lazy { retrofit.create(NotificationApi::class.java) }
}
