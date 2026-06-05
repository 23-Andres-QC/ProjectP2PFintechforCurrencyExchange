package com.example.p2p.presentation.dispute

import com.example.p2p.navigation.Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private data class Dispute(
    val id: String,
    val rawId: String,
    val status: String,
    val statusColor: Color,
    val reason: String,
    val transactionId: String,
    val rawTransactionId: String,
    val date: String,
    val unreadMessages: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDisputesScreen(
    viewModel: DisputesViewModel? = null,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = DisputesUiState())
        ?: remember { mutableStateOf(DisputesUiState()) }
    var selectedFilter by remember { mutableStateOf(0) }
    val filters = listOf("Todas", "Abiertas", "Resueltas")

    val disputes = uiState.disputes.map { dto ->
        val statusName = when (dto.status) {
            "open"         -> "Abierta"
            "under_review" -> "En revisión"
            "resolved"     -> "Resuelta"
            "closed"       -> "Cerrada"
            else           -> dto.status
        }
        val sColor = when (dto.status) {
            "open"         -> DangerColor
            "under_review" -> WarningColor
            "resolved"     -> SuccessColor
            else           -> TextMuted
        }
        val formattedDate = try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = parser.parse(dto.created_at.substringBefore("."))
            if (date != null) formatter.format(date) else dto.created_at.take(10)
        } catch (e: Exception) {
            dto.created_at.take(10)
        }
        Dispute(
            id = "#DSP-${dto.id.takeLast(4).uppercase()}",
            rawId = dto.id,
            status = statusName,
            statusColor = sColor,
            reason = dto.reason,
            transactionId = "#TX-${dto.transaction_id.takeLast(4).uppercase()}",
            rawTransactionId = dto.transaction_id,
            date = formattedDate,
            unreadMessages = 0
        )
    }

    val filteredList = if (selectedFilter == 0) disputes else disputes.filter {
        when (selectedFilter) {
            1 -> it.status == "Abierta"
            2 -> it.status == "Resuelta"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Disputas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextMain,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
            )
        },
        containerColor = BackgroundApp,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Header banner ─────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1A2340), Color(0xFF0D1117))
                            )
                        )
                        .border(
                            1.dp,
                            Primary.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Centro de Disputas",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Text(
                                text = "${disputes.size} disputa${if (disputes.size != 1) "s" else ""} registrada${if (disputes.size != 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Primary.copy(alpha = 0.12f))
                                .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "⚖",
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }

            // ── Filtros ───────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filters.forEachIndexed { index, label ->
                        val isSelected = index == selectedFilter
                        val count = when (index) {
                            1 -> disputes.count { it.status == "Abierta" }
                            2 -> disputes.count { it.status == "Resuelta" }
                            else -> disputes.size
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary.copy(alpha = 0.5f) else BorderColor,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { selectedFilter = index }
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary else TextMuted,
                                )
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(
                                                if (isSelected) Primary.copy(alpha = 0.25f)
                                                else BorderColor
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Primary else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Empty state ───────────────────────────────────────────────
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚖", fontSize = 36.sp)
                            Text(
                                "Sin disputas en esta categoría",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Cards ─────────────────────────────────────────────────────
            items(filteredList.size) { index ->
                DisputeCard(
                    dispute = filteredList[index],
                    onViewDetail = { disputeId ->
                        onNavigate(Screen.DisputeDetail.createRoute(disputeId))
                    }
                )
            }
        }
    }
}

@Composable
private fun DisputeCard(dispute: Dispute, onViewDetail: (String) -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ID + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = dispute.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Primary,
                    )
                    Text(
                        text = dispute.transactionId,
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(dispute.statusColor.copy(alpha = 0.12f))
                        .border(1.dp, dispute.statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = dispute.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dispute.statusColor,
                    )
                }
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            // Motivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dispute.reason,
                        fontSize = 11.sp,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "📅  ${dispute.date}",
                    fontSize = 11.sp,
                    color = TextSubtle,
                )
            }

            // Botón
            OutlinedButton(
                onClick = { onViewDetail(dispute.rawId) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Primary.copy(alpha = 0.06f)
                )
            ) {
                Text(text = "Ver detalle", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}