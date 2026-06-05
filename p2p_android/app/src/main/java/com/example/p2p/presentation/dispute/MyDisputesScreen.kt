package com.example.p2p.presentation.dispute

import com.example.p2p.navigation.Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SuccessColor
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.WarningColor
import java.text.SimpleDateFormat
import java.util.Locale

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDisputesScreen(
    viewModel: DisputesViewModel? = null,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = DisputesUiState()) ?: remember { mutableStateOf(DisputesUiState()) }
    var selectedFilter by remember { mutableStateOf(0) }
    val filters = listOf("Todas", "Abiertas", "Resueltas")

    val disputes = uiState.disputes.map { dto ->
        val statusName = when (dto.status) {
            "open" -> "Abierta"
            "resolved" -> "Resuelta"
            "closed" -> "Cerrada"
            else -> dto.status
        }
        val sColor = when (dto.status) {
            "open" -> DangerColor
            "resolved" -> SuccessColor
            else -> TextMuted
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


            // Filter chips
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filters.forEachIndexed { index, label ->
                        val isSelected = index == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) Primary else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else BorderColor,
                                    shape = RoundedCornerShape(50.dp),
                                )
                                .clickable { selectedFilter = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                            )
                        }
                    }
                }
            }

            // Dispute cards
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

// ---------------------------------------------------------------------------
// Dispute Card
// ---------------------------------------------------------------------------

@Composable
private fun DisputeCard(dispute: Dispute, onViewDetail: (String) -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // TX id + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = dispute.transactionId,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextMain,
                )
                DisputeStatusBadge(label = dispute.status, color = dispute.statusColor)
            }

            Spacer(Modifier.height(8.dp))

            // Opponent -> TX ID
            Text(
                text = dispute.transactionId,
                fontSize = 13.sp,
                color = TextMuted,
            )

            Spacer(Modifier.height(6.dp))

            // Amount + date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dispute.reason,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary,
                )
                Text(
                    text = dispute.date,
                    fontSize = 12.sp,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Ver detalle button
            OutlinedButton(
                onClick = { onViewDetail(dispute.rawId) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp),
            ) {
                Text(text = "Ver detalle", fontSize = 13.sp, color = Primary)
            }
        }
    }
}

@Composable
private fun DisputeStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun MyDisputesScreenPreview() {
    MyDisputesScreen()
}
