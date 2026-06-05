package com.example.p2p.presentation.dispute
import com.example.p2p.data.remote.model.DisputeReason
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeDetailScreen(
    disputeId: String?,
    viewModel: DisputesViewModel? = null,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(DisputesUiState()) }

    LaunchedEffect(disputeId) {
        if (disputeId != null) viewModel?.loadDisputeDetail(disputeId)
    }

    val dispute = uiState.selectedDispute

    val statusLabel = when (dispute?.status) {
        "open"         -> "ABIERTA"
        "under_review" -> "EN REVISIÓN"
        "resolved"     -> "RESUELTA"
        "closed"       -> "CERRADA"
        else           -> dispute?.status?.uppercase() ?: "CARGANDO..."
    }
    val statusColor = when (dispute?.status) {
        "open"         -> DangerColor
        "under_review" -> WarningColor
        "resolved"     -> SuccessColor
        else           -> TextMuted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle de Disputa", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ID + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "#DSP-${(dispute?.id ?: disputeId ?: "").takeLast(4).uppercase()}",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DangerColor
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            // Info de la disputa
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    DetailRow("Transacción:", "#TX-${(dispute?.transaction_id ?: "--").takeLast(4).uppercase()}")
                    DetailRow("Motivo:", DisputeReason.label(dispute?.reason ?: ""))
                    DetailRow("Descripción:", dispute?.description ?: "Sin descripción")
                    DetailRow("Iniciada por:", dispute?.initiator_name ?: dispute?.initiator_id?.take(8)?.uppercase() ?: "--")
                    DetailRow("Fecha:", dispute?.created_at?.take(10) ?: "--")
                }
            }

            // Info de la transacción
            dispute?.transaction?.let { txn ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            "Transacción Asociada",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DetailRow("Monto enviado:", "${String.format("%.2f", txn.amount_from)} USD")
                        DetailRow("Monto recibido:", "S/ ${String.format("%.2f", txn.amount_to)}")
                        DetailRow("Tasa:", "S/ ${txn.exchange_rate}")
                        DetailRow("Estado TX:", txn.status.uppercase(), isLast = true)
                    }
                }
            }

            // Resolución (si existe)
            if (dispute?.status == "resolved") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessColor.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resolución", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
                        Text(
                            when (dispute.resolution) {
                                "favour_buyer"  -> "✓ Resuelta a favor del comprador"
                                "favour_vendor" -> "✓ Resuelta a favor del vendedor"
                                else -> dispute.resolution ?: "--"
                            },
                            fontSize = 13.sp, color = TextMain
                        )
                        dispute.resolution_note?.let {
                            Text(it, fontSize = 12.sp, color = TextMuted)
                        }
                        dispute.resolved_at?.let {
                            Text("Fecha: ${it.take(10)}", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isLast: Boolean = false) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            Text(value, fontSize = 13.sp, color = TextMuted)
        }
        if (!isLast) HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
    }
}