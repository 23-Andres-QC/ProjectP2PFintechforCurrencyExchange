package com.example.p2p.presentation.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.p2p.BuildConfig
import com.example.p2p.core.network.GroqClient
import com.example.p2p.data.local.ChatHistoryStore
import com.example.p2p.data.remote.model.GroqMessage
import com.example.p2p.data.remote.model.GroqRequest
import kotlinx.coroutines.launch
import java.util.UUID

private const val SYSTEM_PROMPT =
    "Eres un asistente virtual de PeruExchange, la mejor plataforma P2P de cambio de divisas en Perú. " +
    "Ayudas a compradores y vendedores con dudas sobre: cómo funciona el cambio de divisas P2P, " +
    "publicación y gestión de ofertas de cambio, tipos de cambio disponibles (PEN, USD, EUR, USDT, COP, MXN, ARS, GBP, BRL, CAD, AUD, JPY, CLP), " +
    "proceso de transacciones paso a paso, seguridad y verificación KYC, " +
    "medios de pago aceptados (BCP, Interbank, BBVA, Yape, Plin), " +
    "matching automático entre compradores y vendedores, y resolución de disputas. " +
    "Responde siempre en español peruano, de forma clara, concisa y amigable. Máximo 3 párrafos cortos por respuesta."

class ChatBotViewModel(private val store: ChatHistoryStore) : ViewModel() {

    val currentMessages = mutableStateListOf<ChatMessage>()
    val conversations   = mutableStateListOf<ChatConversation>()

    var isLoading by mutableStateOf(false)
        private set

    var currentConvId by mutableStateOf(UUID.randomUUID().toString())
        private set

    init {
        viewModelScope.launch {
            conversations.addAll(store.load())
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return
        currentMessages.add(ChatMessage(role = "user", content = text.trim()))

        if (BuildConfig.GROQ_API_KEY.isBlank()) {
            currentMessages.add(
                ChatMessage(
                    role = "assistant",
                    content = "El chatbot no tiene configurada su clave de IA. Revisa el archivo .env e intenta nuevamente."
                )
            )
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val history = currentMessages.takeLast(10)
                    .map { GroqMessage(role = it.role, content = it.content) }
                val msgs = mutableListOf(GroqMessage(role = "system", content = SYSTEM_PROMPT))
                msgs.addAll(history)
                val response = GroqClient.groqApi.chat(
                    authorization = "Bearer ${BuildConfig.GROQ_API_KEY}",
                    request = GroqRequest(messages = msgs)
                )
                val reply = response.choices.firstOrNull()?.message?.content
                    ?: "Lo siento, no pude procesar tu consulta."
                currentMessages.add(ChatMessage(role = "assistant", content = reply))
            } catch (e: Exception) {
                currentMessages.add(
                    ChatMessage(
                        role = "assistant",
                        content = "Error de conexión. Por favor verifica tu internet e intenta de nuevo."
                    )
                )
            }
            isLoading = false
        }
    }

    private fun persistCurrentConversation() {
        if (currentMessages.isEmpty()) return
        val title = currentMessages.firstOrNull { it.role == "user" }
            ?.content?.take(45) ?: "Conversación"
        val conv = ChatConversation(
            id = currentConvId,
            title = title,
            messages = currentMessages.toList()
        )
        val idx = conversations.indexOfFirst { it.id == currentConvId }
        if (idx >= 0) conversations[idx] = conv else conversations.add(0, conv)
        viewModelScope.launch { store.save(conversations.toList()) }
    }

    fun startNewConversation() {
        persistCurrentConversation()
        currentMessages.clear()
        currentConvId = UUID.randomUUID().toString()
    }

    fun loadConversation(conv: ChatConversation) {
        persistCurrentConversation()
        currentMessages.clear()
        currentMessages.addAll(conv.messages)
        currentConvId = conv.id
    }

    fun clearHistory() {
        conversations.clear()
        currentMessages.clear()
        currentConvId = UUID.randomUUID().toString()
        viewModelScope.launch { store.clear() }
    }

    class Factory(private val store: ChatHistoryStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatBotViewModel(store) as T
    }
}
