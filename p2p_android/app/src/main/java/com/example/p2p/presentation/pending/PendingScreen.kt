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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.presentation.vendor.VendorInboxScreen
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.components.GlassIconBadge
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
    val vendorPendingTransactions by viewModel.pendingTransactions.collectAsState()

    RefreshOnResume {
        viewModel.loadBuyerTransactions(currentUserId)
        viewModel.loadPendingTransactions(showLoading = false)
    }

    LaunchedEffect(currentUserId) {
        viewModel.loadBuyerTransactions(currentUserId)
        viewModel.loadPendingTransactions()
    }

    LaunchedEffect(currentUserId, selectedTab) {
        while (true) {
            delay(3000L)
            if (selectedTab == 0) {
                viewModel.loadBuyerTransactions(currentUserId)
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
        ) {
            PendingTabSelector(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                buyCount = buyerTransactions.count { it.status != "completed" },
                sellCount = vendorPendingTransactions.size
            )

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
private fun PendingTabSelector(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    buyCount: Int,
    sellCount: Int
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(4.dp),
        elevation = 4.dp,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PendingTabChip(
                label = "Comprar",
                icon = Icons.Default.ShoppingCart,
                selected = selectedTab == 0,
                count = buyCount,
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(0) }
            )
            PendingTabChip(
                label = "Ventas",
                icon = Icons.Default.Store,
                selected = selectedTab == 1,
                count = sellCount,
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(1) }
            )
        }
    }
}

@Composable
private fun PendingTabChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .then(
                if (selected) {
                    Modifier
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(11.dp), ambientColor = Primary.copy(alpha = 0.4f), spotColor = Primary.copy(alpha = 0.4f))
                        .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
                } else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color.White else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (selected) Color.White else TextMuted
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = 0.28f) else DangerColor)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (count > 99) "99+" else count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                        .background(Primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Primary, modifier = Modifier.size(30.dp))
                }
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
    val (statusLabel, statusIcon, statusColor) = if (isVendor) {
        when (transaction.status) {
            "pending"          -> Triple("Nueva orden de compra · Acepta o rechaza", Icons.Default.Store, Primary)
            "accepted"         -> Triple("Esperando pago del comprador", Icons.Default.Schedule, WarningColor)
            "voucher_uploaded" -> Triple("Comprobante recibido · Confirma el pago", Icons.Default.CheckCircle, Primary)
            "completed"        -> Triple("Fondos liberados · Cierra o disputa", Icons.Default.CheckCircle, SuccessColor)
            else               -> Triple("En curso", Icons.Default.Info, TextMuted)
        }
    } else {
        when (transaction.status) {
            "pending"          -> Triple("Esperando al vendedor", Icons.Default.Schedule, WarningColor)
            "accepted"         -> Triple("Vendedor aceptó · Sube tu comprobante", Icons.Default.CheckCircle, Primary)
            "voucher_uploaded" -> Triple("Verificando tu pago", Icons.Default.Pending, WarningColor)
            "completed"        -> Triple("Fondos liberados · Cierra o disputa", Icons.Default.CheckCircle, SuccessColor)
            else               -> Triple("En curso", Icons.Default.Info, TextMuted)
        }
    }

    val cardTint = when (transaction.status) {
        "completed" -> SuccessColor
        else -> Primary
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        tint = cardTint,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        elevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassIconBadge(icon = statusIcon, tint = statusColor, size = 40.dp, iconSize = 20.dp)
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TextMuted.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isVendor) "VENDEDOR" else "COMPRADOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted,
                        letterSpacing = 0.6.sp
                    )
                }
                Text(
                    text = statusLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${String.format("%.2f", transaction.amount_from)} USD · S/ ${String.format("%.2f", transaction.amount_to)}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            GlassIconBadge(icon = Icons.AutoMirrored.Filled.ArrowForward, tint = statusColor, size = 28.dp, iconSize = 16.dp)
        }
    }
}
