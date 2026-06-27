package com.example.p2p.core.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "p2p_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
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
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            prefs[USER_ID] = userId
            prefs[USER_ROLE] = role
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
        }
        cachedAccessToken = accessToken
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[REFRESH_TOKEN] }.first()

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[USER_ID] }.first()

    suspend fun getUserRole(): String? =
        context.dataStore.data.map { it[USER_ROLE] }.first()

    suspend fun getUserName(): String? =
        context.dataStore.data.map { it[USER_NAME] }.first()

    suspend fun getUserEmail(): String? =
        context.dataStore.data.map { it[USER_EMAIL] }.first()

    suspend fun isLoggedIn(): Boolean = getAccessToken()?.isNotEmpty() == true

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
        cachedAccessToken = null
    }
}
