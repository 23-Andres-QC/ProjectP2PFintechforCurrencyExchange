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
    "Eres el asistente virtual de PeruExchange P2P. Responde solo sobre funciones reales de la app: " +
    "registro, login, KYC, mercado, publicacion de ofertas, cuentas bancarias, compras, comprobantes, " +
    "transacciones, ventas pendientes, disputas, reclamos, calificaciones, notificaciones, ayuda, terminos y privacidad. " +
    "Reglas reales: el vendedor debe tener una cuenta bancaria para publicar y esa cuenta se muestra al comprador; " +
    "la cuenta bancaria del comprador no es obligatoria para comprar; el comprador sube comprobante y el vendedor revisa/libera; " +
    "si hay problema, se puede abrir disputa desde la transaccion o registrar reclamo desde soporte; " +
    "las notificaciones se envian por eventos transaccionales relevantes. " +
    "Medios de pago aceptados: transferencia bancaria (BCP, Interbank, BBVA u otros bancos) o billeteras moviles Yape y Plin; " +
    "cada usuario registra su propia cuenta o numero de celular en la seccion Cuentas Bancarias, " +
    "y el comprador paga directamente a la cuenta que el vendedor registro en su oferta (la app no procesa el pago). " +
    "Matching (o matching automatico): es el boton Matching que aparece en cada oferta del Mercado; " +
    "abre una compra directa e inmediata sobre esa oferta a la tasa que el vendedor ya publico; " +
    "el comprador elige el monto dentro de los limites de la oferta, confirma, sube su comprobante y el vendedor revisa y libera; " +
    "no es un algoritmo que empareja ordenes en segundo plano, es una compra instantanea sobre una oferta visible del mercado. " +
    "No prometas OCR, liberacion automatica, custodia bancaria, tiempos garantizados, bancos no configurados, comisiones no mostradas, soporte 24/7 ni asesoria legal/financiera. " +
    "Si no sabes algo o la app no lo muestra, dilo y sugiere revisar Soporte/Reclamos. " +
    "Responde siempre en espanol peruano, claro y amable, maximo 3 parrafos cortos."

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
