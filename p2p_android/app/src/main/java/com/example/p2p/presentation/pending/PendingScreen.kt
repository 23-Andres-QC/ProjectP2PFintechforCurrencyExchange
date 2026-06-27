package com.example.p2p.presentation.pending

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.presentation.vendor.VendorInboxScreen
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingScreen(
    viewModel: TransactionViewModel,
    currentUserId: String,
    onNavigateToTransaction: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val buyerTransactions by viewModel.buyerTransactions.collectAsState()

    LaunchedEffect(currentUserId) {
        viewModel.loadBuyerTransactions(currentUserId)
        viewModel.loadPendingTransactions()
    }

    LaunchedEffect(currentUserId) {
        while (true) {
            delay(8000L)
            viewModel.loadBuyerTransactions(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendientes", fontWeight = FontWeight.Bold, color = TextMain) },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadBuyerTransactions(currentUserId)
                        viewModel.loadPendingTransactions()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceColor,
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Comprar", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Ventas", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTab) {
                0 -> BuyerPendingList(
                    transactions = buyerTransactions,
                    onClick = onNavigateToTransaction
                )
                else -> VendorInboxScreen(
                    viewModel = viewModel,
                    showTopBar = false
                )
            }
        }
    }
}

@Composable
private fun BuyerPendingList(
    transactions: List<Transaction>,
    onClick: (String) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Inbox, contentDescription = null, tint = BorderColor, modifier = Modifier.size(48.dp))
                Text("Sin compras pendientes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)
                Text(
                    "Tus compras activas aparecerán aquí.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(transactions, key = { it.id }) { txn ->
            ActiveTransactionBanner(
                transaction = txn,
                isVendor = false,
                onClick = { onClick(txn.id) }
            )
        }
    }
}

@Composable
internal fun ActiveTransactionBanner(
    transaction: Transaction,
    isVendor: Boolean = false,
    onClick: () -> Unit
) {
    val (statusLabel, statusIcon) = if (isVendor) {
        when (transaction.status) {
            "pending"          -> "Nueva orden de compra · Acepta o rechaza" to Icons.Default.Store
            "accepted"         -> "Esperando pago del comprador" to Icons.Default.Schedule
            "voucher_uploaded" -> "Comprobante recibido · Confirma el pago" to Icons.Default.CheckCircle
            "completed"        -> "Fondos liberados · Cierra o disputa" to Icons.Default.CheckCircle
            else               -> "En curso" to Icons.Default.Info
        }
    } else {
        when (transaction.status) {
            "pending"          -> "Esperando al vendedor" to Icons.Default.Schedule
            "accepted"         -> "Vendedor aceptó · Sube tu comprobante" to Icons.Default.CheckCircle
            "voucher_uploaded" -> "Verificando tu pago" to Icons.Default.Pending
            "completed"        -> "Fondos liberados · Cierra o disputa" to Icons.Default.CheckCircle
            else               -> "En curso" to Icons.Default.Info
        }
    }

    val roleBadgeColor = if (isVendor) WarningColor else Primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(roleBadgeColor.copy(alpha = 0.07f))
            .border(1.5.dp, roleBadgeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(roleBadgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(statusIcon, contentDescription = null, tint = roleBadgeColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleBadgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isVendor) "VENDEDOR" else "COMPRADOR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = roleBadgeColor,
                    letterSpacing = 0.6.sp
                )
            }
            Text(
                text = statusLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "${String.format("%.2f", transaction.amount_from)} USD · S/ ${String.format("%.2f", transaction.amount_to)}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = roleBadgeColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
