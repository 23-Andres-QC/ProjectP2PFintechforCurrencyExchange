package com.example.p2p.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.p2p.presentation.chatbot.ChatConversation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

class ChatHistoryStore(private val context: Context) {

    private val gson = Gson()
    private val KEY = stringPreferencesKey("conversations")

    private val flow: Flow<List<ChatConversation>> = context.chatDataStore.data.map { prefs ->
        try {
            val json = prefs[KEY] ?: return@map emptyList()
            val type = object : TypeToken<List<ChatConversation>>() {}.type
            gson.fromJson<List<ChatConversation>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun load(): List<ChatConversation> = flow.first()

    suspend fun save(conversations: List<ChatConversation>) {
        context.chatDataStore.edit { it[KEY] = gson.toJson(conversations) }
    }

    suspend fun clear() {
        context.chatDataStore.edit { it.remove(KEY) }
    }
}