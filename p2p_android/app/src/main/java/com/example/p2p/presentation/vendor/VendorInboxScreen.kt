package com.example.p2p.presentation.vendor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorInboxScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var confirmAcceptTxnId by remember { mutableStateOf<String?>(null) }
    var confirmCancelTxnId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingTransactions()
    }

    // Auto-refresh every 5 seconds to catch new buyer orders
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            viewModel.loadPendingTransactions()
        }
    }

    confirmAcceptTxnId?.let { txnId ->
        AlertDialog(
            onDismissRequest = { confirmAcceptTxnId = null },
            title = {
                Text(
                    "¿Estás seguro?",
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            },
            text = {
                Text(
                    "¿Deseas aceptar esta orden de compra? El comprador será notificado para realizar el pago.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = txnId
                        confirmAcceptTxnId = null
                        viewModel.acceptTransaction(id,
                            onSuccess = { Toast.makeText(context, "Orden aceptada. El comprador fue notificado.", Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, "Error al aceptar: $err", Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Sí, aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAcceptTxnId = null }) {
                    Text("No", color = TextMuted)
                }
            },
            containerColor = SurfaceColor
        )
    }

    confirmCancelTxnId?.let { txnId ->
        AlertDialog(
            onDismissRequest = { confirmCancelTxnId = null },
            title = {
                Text(
                    "¿Estás seguro?",
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            },
            text = {
                Text(
                    "¿Deseas rechazar esta orden de compra? La operación será cancelada.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = txnId
                        confirmCancelTxnId = null
                        viewModel.cancelTransaction(id,
                            onSuccess = { Toast.makeText(context, "Orden rechazada.", Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, "Error al rechazar: $err", Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                ) {
                    Text("Sí, rechazar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancelTxnId = null }) {
                    Text("No", color = TextMuted)
                }
            },
            containerColor = SurfaceColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pendientes",
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextMain)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPendingTransactions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ÓRDENES ACTIVAS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarningColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Órdenes Pendientes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Acepta, cancela o confirma los pagos activos",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarningColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = WarningColor)
                    }
                }
            }

            if (uiState.isLoading && pendingTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (pendingTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = BorderColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Sin órdenes activas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextMain
                        )
                        Text(
                            text = "Las órdenes pendientes de acción aparecerán aquí. El historial completo está en tu perfil.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingTransactions) { txn ->
                        VendorTransactionCard(
                            transaction = txn,
                            onAccept = {
                                confirmAcceptTxnId = txn.id
                            },
                            onConfirm = {
                                viewModel.confirmTransaction(txn.id,
                                    onSuccess = { Toast.makeText(context, "Operación liberada con éxito", Toast.LENGTH_SHORT).show() },
                                    onError = { err -> Toast.makeText(context, "Error al confirmar: $err", Toast.LENGTH_LONG).show() }
                                )
                            },
                            onCancel = {
                                confirmCancelTxnId = txn.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorTransactionCard(
    transaction: Transaction,
    onAccept: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit = {}
) {
    val isNewOrder = transaction.status == "pending"
    val isVoucherUploaded = transaction.status == "voucher_uploaded"
    val isAccepted = transaction.status == "accepted"

    val statusLabel = when (transaction.status) {
        "pending"          -> "NUEVA ORDEN DE COMPRA"
        "accepted"         -> "ORDEN ACEPTADA · ESPERANDO PAGO"
        "voucher_uploaded" -> "PAGO RECIBIDO · CONFIRMAR"
        else               -> transaction.status.uppercase()
    }
    val statusColor = when (transaction.status) {
        "pending"          -> Primary
        "accepted"         -> SuccessColor
        "voucher_uploaded" -> WarningColor
        else               -> TextMuted
    }
    val cardBorder = when (transaction.status) {
        "pending"          -> Primary.copy(alpha = 0.4f)
        "accepted"         -> SuccessColor.copy(alpha = 0.3f)
        "voucher_uploaded" -> WarningColor.copy(alpha = 0.3f)
        else               -> BorderColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = transaction.id.take(8).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = transaction.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // New order banner
            if (isNewOrder) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.07f))
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Un comprador quiere comprarte divisas. Acepta para confirmar la operación.",
                        fontSize = 11.sp,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundApp)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Comprador", fontSize = 12.sp, color = TextMuted)
                    Text(
                        transaction.buyer_name ?: transaction.buyer_id.take(8).uppercase(),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monto", fontSize = 12.sp, color = TextMuted)
                    Text(
                        "S/ ${String.format("%.2f", transaction.amount_to)} · tasa ${transaction.exchange_rate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
            }

            if (isNewOrder) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Aceptar orden de compra", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rechazar orden", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isAccepted) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SuccessColor.copy(alpha = 0.07f))
                            .border(1.dp, SuccessColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Orden aceptada. Esperando que el comprador realice el pago y suba su comprobante.",
                            fontSize = 11.sp,
                            color = SuccessColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar operación", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isVoucherUploaded) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Confirmar Pago y Liberar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
