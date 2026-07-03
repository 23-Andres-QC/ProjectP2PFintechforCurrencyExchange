package com.example.p2p.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TokenManager(private val context: Context) {

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val USER_ID = "user_id"
        private const val USER_ROLE = "user_role"
        private const val USER_NAME = "user_name"
        private const val USER_EMAIL = "user_email"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
    }

    // Guardado con androidx.security (Android Keystore) en vez de DataStore plano:
    // los tokens de sesion de una app financiera no deben quedar en texto plano en disco.
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "p2p_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Cache en memoria para que el interceptor HTTP pueda leer el token sin bloquear
    // en cada request. Se precarga una vez en background al construir el singleton
    // (sin runBlocking: bloquear el hilo principal en onCreate causaba ANR en frío).
    @Volatile
    private var cachedAccessToken: String? = null

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            cachedAccessToken = getAccessToken()
        }
    }

    fun peekAccessToken(): String? = cachedAccessToken

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        role: String,
        name: String,
        email: String
    ) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(ACCESS_TOKEN, accessToken)
            .putString(REFRESH_TOKEN, refreshToken)
            .putString(USER_ID, userId)
            .putString(USER_ROLE, role)
            .putString(USER_NAME, name)
            .putString(USER_EMAIL, email)
            .apply()
        cachedAccessToken = accessToken
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(ACCESS_TOKEN, null)
    }

    suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(REFRESH_TOKEN, null)
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        prefs.getString(USER_ID, null)
    }

    suspend fun getUserRole(): String? = withContext(Dispatchers.IO) {
        prefs.getString(USER_ROLE, null)
    }

    suspend fun getUserName(): String? = withContext(Dispatchers.IO) {
        prefs.getString(USER_NAME, null)
    }

    suspend fun getUserEmail(): String? = withContext(Dispatchers.IO) {
        prefs.getString(USER_EMAIL, null)
    }

    suspend fun isLoggedIn(): Boolean = getAccessToken()?.isNotEmpty() == true

    suspend fun clearSession() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        cachedAccessToken = null
    }
}
