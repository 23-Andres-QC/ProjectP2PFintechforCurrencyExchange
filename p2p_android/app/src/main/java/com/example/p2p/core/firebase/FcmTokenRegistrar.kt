package com.example.p2p.core.firebase

import android.content.Context
import android.util.Log
import com.example.p2p.core.network.ApiClient
import com.example.p2p.core.security.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

private const val FCM_TAG = "FcmTokenRegistrar"

object FcmTokenRegistrar {

    suspend fun registerIfLoggedIn(context: Context): Boolean {
        return try {
            val tokenManager = TokenManager.getInstance(context.applicationContext)
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                Log.d(FCM_TAG, "Sesion no iniciada; token FCM pendiente.")
                return false
            }

            val fcmToken = FirebaseMessaging.getInstance().token.await()
            val response = ApiClient.notificationApi.registerFcmToken(
                mapOf("fcm_token" to fcmToken)
            )
            if (!response.isSuccessful) {
                Log.w(FCM_TAG, "FCM token no registrado: HTTP ${response.code()}")
                return false
            }

            Log.d(FCM_TAG, "FCM token registrado correctamente.")
            true
        } catch (e: Exception) {
            Log.w(FCM_TAG, "No se pudo registrar el token FCM", e)
            false
        }
    }
}
