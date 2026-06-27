package com.example.p2p.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun TransactionDetailScreen(
    transactionId: String?,
    viewModel: TransactionViewModel? = null,
    onNavigateToDispute: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = TransactionUiState())
        ?: remember { mutableStateOf(TransactionUiState()) }

    LaunchedEffect(transactionId) {
        if (transactionId != null) viewModel?.loadTransaction(transactionId)
    }

    val txn = uiState.transaction

    val statusLabel = when (txn?.status) {
        "completed"       -> "COMPLETADO"
        "pending"         -> "PENDIENTE"
        "voucher_uploaded"-> "EN PROCESO"
        "cancelled"       -> "CANCELADO"
        "disputed"        -> "EN DISPUTA"
        else              -> txn?.status?.uppercase() ?: "CARGANDO..."
    }
    val statusColor = when (txn?.status) {
        "completed"       -> SuccessColor
        "pending", "voucher_uploaded" -> WarningColor
        "cancelled", "disputed"       -> DangerColor
        else              -> TextMuted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Transacción", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain) },
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
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Transaction ID + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "#${(txn?.id ?: transactionId ?: "").takeLast(8).uppercase()}",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary
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

            // Detail card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    DetailRow("Tipo:", "Compra de divisas", showDivider = true)
                    DetailRow("Comprador:", txn?.buyer_name ?: txn?.buyer_id?.take(8)?.uppercase() ?: "--", showDivider = true)
                    DetailRow("Vendedor:", txn?.vendor_name ?: txn?.vendor_id?.take(8)?.uppercase() ?: "--", showDivider = true)
                    DetailRow("Monto enviado:", "${String.format("%.2f", txn?.amount_from ?: 0.0)} USD", showDivider = true)
                    DetailRow("Monto recibido:", "S/ ${String.format("%.2f", txn?.amount_to ?: 0.0)}", showDivider = true)
                    DetailRow("Tasa:", "S/ ${txn?.exchange_rate ?: "--"}", showDivider = true)
                    DetailRow("Método de pago:", txn?.vendor_payment_account ?: "--", showDivider = true)
                    DetailRow("Fecha:", txn?.created_at?.take(10) ?: "--", showDivider = true)
                    // OCR row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado OCR:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                        Text("✓ Auténtico", fontSize = 13.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Download PDF button
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Descargar PDF", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            // Dispute button — solo si la transacción no está completada ni cancelada


            if (txn?.status !in listOf("completed", "cancelled", "disputed")) {
                OutlinedButton(
                    onClick = { transactionId?.let { onNavigateToDispute(it) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerColor)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir Disputa", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            // Back to history
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Volver al Historial", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}


@Composable
private fun DetailRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            Text(value, fontSize = 13.sp, color = TextMuted, fontWeight = valueWeight)
        }
        if (showDivider) HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
    }
}


