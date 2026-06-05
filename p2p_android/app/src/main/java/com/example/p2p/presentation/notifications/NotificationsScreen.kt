package com.example.p2p.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notificaciones", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 18.sp)
                        if (state.unreadCount > 0) {
                            Text(
                                "${state.unreadCount} sin leer",
                                fontSize = 12.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
            )
        },
        containerColor = BackgroundApp,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                        Text("Cargando notificaciones...", color = TextMuted, fontSize = 13.sp)
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(DangerColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = DangerColor, modifier = Modifier.size(32.dp))
                        }
                        Text("No se pudo cargar", fontWeight = FontWeight.SemiBold, color = TextMain, fontSize = 15.sp)
                        Text("Revisa tu conexión e inténtalo de nuevo.", color = TextMuted, fontSize = 13.sp)
                        Button(
                            onClick = { viewModel.loadAndMarkRead() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }

                state.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Primary, modifier = Modifier.size(38.dp))
                        }
                        Text("Todo al día", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                        Text(
                            "Aquí aparecerán los eventos de tu cuenta: transacciones, disputas y más.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.notifications, key = { it.id }) { notif ->
                            NotificationCard(
                                item = notif,
                                onDelete = { viewModel.deleteNotification(notif.id) },
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// ── Notification Card con swipe ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(item: Notification, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            // Fondo rojo que aparece al deslizar a la derecha
            val progress = dismissState.progress
            val alpha = (progress * 2f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DangerColor.copy(alpha = alpha)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.padding(start = 20.dp).size(22.dp),
                )
            }
        },
    ) {
        val (icon, accentColor) = notifIconAndColor(item.type)
        val bgColor = if (!item.is_read) Color(0xFFF0F4FF) else SurfaceColor

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (!item.is_read) 2.dp else 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icono
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(21.dp))
                }

                // Contenido
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontWeight = if (!item.is_read) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextMain,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(formatRelativeTime(item.created_at), fontSize = 11.sp, color = TextMuted)
                    }
                    Text(item.body, fontSize = 13.sp, color = TextMuted, lineHeight = 18.sp)
                }

                // Punto azul de no leído
                if (!item.is_read) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Primary),
                    )
                }
            }

            // Barra de acento izquierda para no leídas
            if (!item.is_read) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(accentColor.copy(alpha = 0.4f)),
                )
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun notifIconAndColor(type: String): Pair<ImageVector, Color> = when (type) {
    "login"       -> Icons.Default.CheckCircle          to SuccessColor
    "transaction" -> Icons.Default.SwapHoriz            to Primary
    "voucher"     -> Icons.Default.Description          to WarningColor
    "dispute"     -> Icons.Default.Gavel                to WarningColor
    "offer"       -> Icons.Default.Campaign             to PrimaryLight
    "admin"       -> Icons.Default.AdminPanelSettings   to DangerColor
    "security"    -> Icons.Default.Lock                 to DangerColor
    else          -> Icons.Default.Notifications        to TextMuted
}

private fun formatRelativeTime(isoDate: String): String {
    return try {
        // Soporta tanto "2026-06-04T23:47:44" como "2026-06-04T23:47:44.123456"
        val clean = isoDate.substringBefore('.')
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(clean) ?: return isoDate.take(10)
        val diffMs   = System.currentTimeMillis() - date.time
        val diffMin  = diffMs / 60_000
        val diffHour = diffMin / 60
        val diffDay  = diffHour / 24
        when {
            diffMin  < 1  -> "ahora"
            diffMin  < 60 -> "hace ${diffMin}m"
            diffHour < 24 -> "hace ${diffHour}h"
            diffDay  == 1L -> "ayer"
            diffDay  < 7  -> "hace ${diffDay}d"
            else          -> isoDate.take(10)
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}
