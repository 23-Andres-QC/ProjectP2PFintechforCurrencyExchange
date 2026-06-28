package com.example.p2p.presentation.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Notification
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay

private enum class NotifFilter { ALL, UNREAD }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var filter by remember { mutableStateOf(NotifFilter.ALL) }

    val displayed = remember(state.notifications, filter) {
        if (filter == NotifFilter.UNREAD) state.notifications.filter { !it.is_read }
        else state.notifications
    }

    val grouped = remember(displayed) { groupByDate(displayed) }

    RefreshOnResume {
        viewModel.loadAndMarkRead(showLoading = false)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            viewModel.loadAndMarkRead(showLoading = false)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {

        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(PrimaryDark, Primary))
                )
        ) {
            Column(modifier = Modifier.padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Notificaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.loadAndMarkRead() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(
                        label = "Total",
                        value = "${state.notifications.size}",
                        icon = Icons.Default.Notifications,
                    )
                    if (state.unreadCount > 0) {
                        StatChip(
                            label = "Sin leer",
                            value = "${state.unreadCount}",
                            icon = Icons.Default.FiberManualRecord,
                            highlight = true,
                        )
                    }
                }
            }
        }

        // ── Filter chips ────────────────────────────────────────────────────
        if (state.notifications.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter == NotifFilter.ALL,
                    onClick = { filter = NotifFilter.ALL },
                    label = { Text("Todas", fontSize = 13.sp) },
                    leadingIcon = if (filter == NotifFilter.ALL) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
                FilterChip(
                    selected = filter == NotifFilter.UNREAD,
                    onClick = { filter = NotifFilter.UNREAD },
                    label = { Text("Sin leer", fontSize = 13.sp) },
                    leadingIcon = if (filter == NotifFilter.UNREAD) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                    ),
                )
            }
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
        }

        // ── Content ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp),
                        )
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
                            modifier = Modifier.size(72.dp).clip(CircleShape)
                                .background(DangerColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = DangerColor,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                        Text("Sin conexión", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                        Text(
                            "No se pudo cargar tus notificaciones.\nRevisa tu conexión e inténtalo de nuevo.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                        Button(
                            onClick = { viewModel.loadAndMarkRead() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }

                displayed.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(88.dp).clip(CircleShape)
                                .background(Primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (filter == NotifFilter.UNREAD) Icons.Default.DoneAll
                                else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(42.dp),
                            )
                        }
                        Text(
                            if (filter == NotifFilter.UNREAD) "Todo al día" else "Sin notificaciones",
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            fontSize = 17.sp,
                        )
                        Text(
                            if (filter == NotifFilter.UNREAD)
                                "No tienes notificaciones pendientes por leer."
                            else
                                "Aquí aparecerán tus transacciones, disputas y alertas de cuenta.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        grouped.forEach { (section, items) ->
                            item(key = "header_$section") {
                                Text(
                                    section,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                )
                            }
                            items(items, key = { it.id }) { notif ->
                                NotificationCard(
                                    item = notif,
                                    onDelete = { viewModel.deleteNotification(notif.id) },
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, icon: ImageVector, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (highlight) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

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
            val progress = dismissState.progress
            val alpha = (progress * 2.5f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DangerColor.copy(alpha = alpha * 0.9f)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.White.copy(alpha = alpha), modifier = Modifier.size(20.dp))
                    if (alpha > 0.6f) {
                        Text("Eliminar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
    ) {
        val (icon, accentColor) = notifIconAndColor(item.type)

        GlassCard(
            shape = RoundedCornerShape(14.dp),
            tint = Primary,
            elevation = if (!item.is_read) 4.dp else 1.dp,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {

                // Left accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                        .background(if (!item.is_read) accentColor else Color.Transparent),
                )

                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {

                    // Icon
                    com.example.p2p.ui.components.GlassIconBadge(icon = icon, tint = accentColor, size = 40.dp, iconSize = 20.dp)

                    // Text content
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = item.title,
                                fontWeight = if (!item.is_read) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextMain,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                formatRelativeTime(item.created_at),
                                fontSize = 11.sp,
                                color = TextSubtle,
                            )
                        }
                        Text(
                            item.body,
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!item.is_read) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp),
                            ) {
                                Box(
                                    modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor)
                                )
                                Text("Nuevo", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun notifIconAndColor(type: String): Pair<ImageVector, Color> = when (type) {
    "login"       -> Icons.Default.CheckCircle        to SuccessColor
    "transaction" -> Icons.Default.SwapHoriz          to Primary
    "voucher"     -> Icons.Default.Description        to WarningColor
    "dispute"     -> Icons.Default.Gavel              to DangerColor
    "offer"       -> Icons.Default.Campaign           to PrimaryLight
    "admin"       -> Icons.Default.AdminPanelSettings to Color(0xFF7C3AED)
    "security"    -> Icons.Default.Lock               to DangerColor
    "kyc"         -> Icons.Default.VerifiedUser       to SuccessColor
    else          -> Icons.Default.Notifications      to TextMuted
}

private fun groupByDate(notifications: List<Notification>): Map<String, List<Notification>> {
    val now = System.currentTimeMillis()
    val result = linkedMapOf<String, MutableList<Notification>>()

    notifications.forEach { notif ->
        val section = try {
            val clean = notif.created_at.substringBefore('.')
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(clean)
            val diffDay = if (date != null) ((now - date.time) / 86_400_000).toInt() else 99
            when {
                diffDay == 0 -> "HOY"
                diffDay == 1 -> "AYER"
                diffDay < 7  -> "ESTA SEMANA"
                diffDay < 30 -> "ESTE MES"
                else         -> "ANTERIORES"
            }
        } catch (_: Exception) { "ANTERIORES" }

        result.getOrPut(section) { mutableListOf() }.add(notif)
    }
    return result
}

private fun formatRelativeTime(isoDate: String): String {
    return try {
        val clean = isoDate.substringBefore('.')
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(clean) ?: return isoDate.take(10)
        val diffMs   = System.currentTimeMillis() - date.time
        val diffMin  = diffMs / 60_000
        val diffHour = diffMin / 60
        val diffDay  = diffHour / 24
        when {
            diffMin  < 1   -> "ahora"
            diffMin  < 60  -> "${diffMin}m"
            diffHour < 24  -> "${diffHour}h"
            diffDay  == 1L -> "ayer"
            diffDay  < 7   -> "${diffDay}d"
            else           -> isoDate.take(10)
        }
    } catch (_: Exception) { isoDate.take(10) }
}
