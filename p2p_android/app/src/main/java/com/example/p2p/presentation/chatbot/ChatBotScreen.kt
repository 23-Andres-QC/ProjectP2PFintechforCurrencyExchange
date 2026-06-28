package com.example.p2p.presentation.chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.R
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DarkSurface2
import com.example.p2p.ui.theme.DividerColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.SurfaceElevated
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.TextSecond
import com.example.p2p.ui.theme.TextSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatConversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage>,
    val timestamp: Long = System.currentTimeMillis()
)

private val FAQ_QUESTIONS = listOf(
    "¿Cómo funciona el cambio de divisas P2P?",
    "¿Cómo compro dólares en PeruExchange?",
    "¿Cómo publico una oferta de cambio?",
    "¿Qué medios de pago aceptan?",
    "¿Es seguro operar en la plataforma?",
    "¿Qué hago si hay un problema con mi transacción?",
    "¿Cómo funciona el matching automático?"
)

@Composable
fun ChatBotScreen(viewModel: ChatBotViewModel, onBack: () -> Unit) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.currentMessages.size, viewModel.isLoading) {
        if (viewModel.currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.currentMessages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            Surface(shadowElevation = 3.dp, color = SurfaceColor) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showDrawer = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Primary)
                    }
                    Image(
                        painter = painterResource(R.drawable.ic_chatbot),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PeruExchange AI", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                        Text("Asistente virtual", fontSize = 11.sp, color = TextMuted)
                    }
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }
            }

            // ── Chat area ─────────────────────────────────────────────
            if (viewModel.currentMessages.isEmpty() && !viewModel.isLoading) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_chatbot),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "¡Hola! Soy tu asistente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextMain,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Preguntas frecuentes",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextSecond
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    items(FAQ_QUESTIONS) { faq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { viewModel.sendMessage(faq); inputText = "" }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Article,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.60f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(faq, fontSize = 13.sp, color = Color.Black, modifier = Modifier.weight(1f))
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    items(viewModel.currentMessages, key = { it.id }) { msg ->
                        MessageBubble(msg)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (viewModel.isLoading) {
                        item {
                            TypingIndicator()
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            // ── Input bar ─────────────────────────────────────────────
            Surface(color = SurfaceColor, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe tu pregunta...", fontSize = 14.sp, color = TextSubtle) },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SurfaceColor,
                            unfocusedContainerColor = SurfaceElevated
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    val canSend = inputText.isNotBlank() && !viewModel.isLoading
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Primary else BorderColor)
                            .clickable(enabled = canSend) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // ── Drawer scrim ──────────────────────────────────────────────
        AnimatedVisibility(visible = showDrawer, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { showDrawer = false }
            )
        }

        // ── Drawer panel ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = showDrawer,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Surface(modifier = Modifier.fillMaxHeight().width(285.dp), color = SurfaceColor, shadowElevation = 16.dp) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier.fillMaxWidth().background(Primary).padding(horizontal = 16.dp, vertical = 22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painter = painterResource(R.drawable.ic_chatbot), contentDescription = null, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("PeruExchange AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("Asistente virtual", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Primary.copy(alpha = 0.08f))
                            .clickable { viewModel.startNewConversation(); showDrawer = false }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Iniciar nueva conversación", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Primary)
                    }

                    Spacer(Modifier.height(18.dp))

                    if (viewModel.conversations.isNotEmpty()) {
                        Text("Recientes", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(6.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(viewModel.conversations, key = { it.id }) { conv ->
                                val isActive = conv.id == viewModel.currentConvId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isActive) Primary.copy(alpha = 0.07f) else Color.Transparent)
                                        .clickable { viewModel.loadConversation(conv); showDrawer = false }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = if (isActive) Primary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            conv.title,
                                            fontSize = 13.sp,
                                            color = if (isActive) Primary else TextSecond,
                                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                        Text(
                                            SimpleDateFormat("dd/MM  HH:mm", Locale.getDefault()).format(Date(conv.timestamp)),
                                            fontSize = 10.sp,
                                            color = TextSubtle
                                        )
                                    }
                                }
                                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Sin conversaciones recientes", fontSize = 12.sp, color = TextSubtle, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(painter = painterResource(R.drawable.ic_chatbot), contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            color = if (isUser) Primary else SurfaceColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            shadowElevation = if (isUser) 0.dp else 2.dp,
            modifier = Modifier.widthIn(max = 265.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 14.sp,
                color = if (isUser) Color.White else TextMain,
                lineHeight = 20.sp
            )
        }
        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(R.drawable.ic_chatbot), contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(8.dp))
        Surface(
            color = SurfaceColor,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val transition = rememberInfiniteTransition(label = "dot$i")
                    val offsetY by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 400, delayMillis = i * 130, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "offset$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(y = offsetY.dp)
                            .clip(CircleShape)
                            .background(Primary)
                    )
                }
            }
        }
    }
}