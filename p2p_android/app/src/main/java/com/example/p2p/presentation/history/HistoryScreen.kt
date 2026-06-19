package com.example.p2p.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.*

private data class Transaction(
    val rawId: String,
    val id: String,
    val status: String,
    val statusColor: Color,
    val from: String,
    val to: String,
    val amount: String,
    val rate: String,
    val date: String,
    val icon: ImageVector,
    val isBuyerActive: Boolean = false
)

private val sampleTransactions = listOf(
    Transaction("", "#TX-9982", "Completado", SuccessColor, "Carlos", "Victor",  "$ 200.00 USD", "S/ 3.780", "25 May 2026", Icons.Default.SwapHoriz),
    Transaction("", "#TX-9881", "Completado", SuccessColor, "Carlos", "Ana",     "€ 150.00 EUR", "S/ 4.110", "24 May 2026", Icons.Default.SwapHoriz),
    Transaction("", "#TX-9756", "Pendiente",  WarningColor, "Carlos", "Luis",    "$ 500.00 USD", "S/ 3.775", "23 May 2026", Icons.Default.Schedule),
    Transaction("", "#TX-9654", "Disputa",    DangerColor,  "Carlos", "María",   "$ 100.00 USD", "S/ 3.780", "20 May 2026", Icons.Default.Gavel),
    Transaction("", "#TX-9521", "Completado", SuccessColor, "Carlos", "Víctor",  "$ 300.00 USD", "S/ 3.780", "18 May 2026", Icons.Default.SwapHoriz)
)

private val filterChips = listOf("Todos", "Completados", "Pendientes", "Disputas")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel? = null,
    currentUserId: String = "",
    initialFilter: Int = 0,
    onBack: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToTransactionDetail: (String) -> Unit = {},
    onNavigateToPending: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = HistoryUiState()) ?: remember { mutableStateOf(HistoryUiState()) }
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel?.loadTransactions()
    }

    val transactions = uiState.transactions.map { dto ->
        val statusName = when (dto.status) {
            "completed" -> "Completado"
            "pending" -> "Pendiente"
            "accepted" -> "Aceptado"
            "voucher_uploaded" -> "En Proceso"
            "cancelled" -> "Cancelado"
            "disputed" -> "Disputa"
            else -> dto.status
        }
        val sColor = when (dto.status) {
            "completed" -> SuccessColor
            "pending", "accepted", "voucher_uploaded" -> WarningColor
            "cancelled", "disputed" -> DangerColor
            else -> TextMuted
        }
        val icon = when (dto.status) {
            "completed" -> Icons.Default.SwapHoriz
            "pending", "accepted", "voucher_uploaded" -> Icons.Default.Schedule
            "cancelled" -> Icons.Default.Cancel
            "disputed" -> Icons.Default.Gavel
            else -> Icons.Default.Info
        }
        val formattedDate = try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = parser.parse(dto.created_at.substringBefore("."))
            if (date != null) formatter.format(date) else dto.created_at.take(10)
        } catch (e: Exception) {
            dto.created_at.take(10)
        }

        Transaction(
            rawId = dto.id,
            id = "#TX-${dto.id.takeLast(4).uppercase()}",
            status = statusName,
            statusColor = sColor,
            from = dto.buyer_name ?: dto.buyer_id.take(6).uppercase(),
            to = dto.vendor_name ?: dto.vendor_id.take(6).uppercase(),
            amount = "${String.format("%.2f", dto.amount_from)} USD",
            rate = "S/ ${String.format("%.3f", dto.exchange_rate)}",
            date = formattedDate,
            icon = icon,
            isBuyerActive = currentUserId.isNotBlank() &&
                currentUserId == dto.buyer_id &&
                dto.status in listOf("pending", "accepted", "voucher_uploaded", "completed")
        )
    }

    val filteredList = transactions
        .filter {
            when (selectedFilter) {
                1 -> it.status == "Completado"
                2 -> it.status == "Pendiente" || it.status == "Aceptado" || it.status == "En Proceso"
                3 -> it.status == "Disputa"
                else -> true
            }
        }
        .filter { tx ->
            if (searchQuery.isBlank()) true
            else tx.id.contains(searchQuery, ignoreCase = true) ||
                tx.from.contains(searchQuery, ignoreCase = true) ||
                tx.to.contains(searchQuery, ignoreCase = true)
        }

    Scaffold(
        containerColor = BackgroundApp,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de Operaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por ID o vendedor...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }}
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedContainerColor = SurfaceColor
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(filterChips.indices.toList()) { index ->
                    val isSelected = index == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = index },
                        label = {
                            Text(
                                filterChips[index],
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceColor,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Primary,
                            borderColor = BorderColor
                        )
                    )
                }
            }

            if (filteredList.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = BorderColor,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            "Sin operaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextMain
                        )
                        Text(
                            "Tus transacciones aparecerán aquí",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                    }
                    items(filteredList) { tx ->
                        TransactionCard(tx, onNavigateToTransaction, onNavigateToTransactionDetail)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    tx: Transaction,
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToTransactionDetail: (String) -> Unit = {}
) {
    val isActive = tx.status == "Pendiente" || tx.status == "Aceptado" || tx.status == "En Proceso"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) WarningColor.copy(alpha = 0.5f) else BorderColor
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (tx.rawId.isNotBlank()) Modifier.clickable {
                    if (tx.isBuyerActive) onNavigateToTransaction(tx.rawId)
                    else onNavigateToTransactionDetail(tx.rawId)
                }
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(tx.statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        tx.icon,
                        contentDescription = null,
                        tint = tx.statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(tx.id, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)
                    if (tx.isBuyerActive) {
                        val (label, labelColor) = if (tx.status == "Completado")
                            "Toca para calificar →" to SuccessColor
                        else
                            "Toca para continuar →" to WarningColor
                        Text(label, fontSize = 10.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
                    }
                }
                StatusBadge(tx.status, tx.statusColor)
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PartyAvatar(tx.from.take(2).uppercase(), Primary)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    PartyAvatar(tx.to.take(2).uppercase(), PrimaryLight)
                    Text("${tx.from} → ${tx.to}", fontSize = 12.sp, color = TextMuted)
                }
                Text(tx.amount, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextMain)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    Text("Tipo cambio: ${tx.rate}", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Text(tx.date, fontSize = 11.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PartyAvatar(initials: String, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}
