package com.example.p2p.presentation.admin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.p2p.navigation.Screen
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.DarkSurface
import com.example.p2p.ui.theme.InfoColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.PrimaryMint
import com.example.p2p.ui.theme.SuccessColor
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.SurfaceElevated
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.TextSubtle
import com.example.p2p.ui.theme.WarningColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit = {},  // ← agrega esto
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Disputas", "Reclamos")

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel Administrador",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = TextMain,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                ),
            )
        },
        containerColor = BackgroundApp,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Admin header card
            item {
                AdminHeaderCard(
                    volume = uiState.stats?.total_volume ?: 0.0,
                    disputesCount = uiState.stats?.pending_disputes ?: 0,
                    usersCount = uiState.stats?.total_users ?: 0,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Status pills
            item {
                StatusPillsRow(
                    openCount = uiState.disputes.count { it.status == "open" },
                    reviewCount = uiState.disputes.count { it.status == "under_review" },
                    resolvedCount = uiState.disputes.count { it.status == "resolved" },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Filter tabs
            item {
                Spacer(Modifier.height(16.dp))
                FilterTabsRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }


            // Content by tab
            when (selectedTab) {
                0 -> {
                    // ── Disputas ──────────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Disputas Activas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextMain,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (uiState.isLoading && uiState.disputes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                    } else if (uiState.disputes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay disputas registradas", color = TextMuted)
                            }
                        }
                    } else {
                        items(uiState.disputes) { dispute ->
                            DisputeCard(
                                dispute = dispute,
                                onViewDetail = { disputeId ->
                                    onNavigate(Screen.DisputeDetail.createRoute(disputeId))
                                },
                                onResolve = { resolution ->
                                    viewModel.resolveDispute(
                                        disputeId = dispute.id,
                                        resolution = resolution,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Disputa resuelta :3",
                                                Toast.LENGTH_SHORT
                       ).show()
                    },
                    onError = { err ->
                        Toast.makeText(
                            context,
                            "Error: $err",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    )
                },
            modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    1 -> {
        // ── Reclamos ───────────────────────────────────────────────
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Reclamos de Usuarios",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextMain,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (uiState.isLoading && uiState.complaints.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                    } else if (uiState.complaints.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay reclamos registrados", color = TextMuted)
                            }
                        }
                    } else {
                        items(uiState.complaints) { complaint ->
                            ComplaintAdminCard(
                                complaint = complaint,
                                onResolve = { adminNote ->
                                    viewModel.resolveComplaint(
                                        complaintId = complaint.id,
                                        adminNote = adminNote,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Reclamo resuelto",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(
                                                context,
                                                "Error: $err",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeaderCard(
    volume: Double,
    disputesCount: Int,
    usersCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A2340), Color(0xFF0D1117)),
                )
            )
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "⬡  CONTROL DE OPERACIONES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B),
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Panel Administrador",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "Perú Exchange · Tiempo real",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0F1E).copy(alpha = 0.6f))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdminStat(
                    value = "S/ ${String.format(Locale.getDefault(), "%.1fK", volume / 1000)}",
                    label = "Volumen",
                    valueColor = Color(0xFFF59E0B)
                )
                StatDivider()
                AdminStat(
                    value = disputesCount.toString(),
                    label = "Disputas",
                    valueColor = Color(0xFFEF4444)
                )
                StatDivider()
                AdminStat(
                    value = usersCount.toString(),
                    label = "Usuarios",
                    valueColor = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun AdminStat(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(38.dp)
            .background(Color.White.copy(alpha = 0.2f)),
    )
}

@Composable
private fun StatusPillsRow(
    openCount: Int,
    reviewCount: Int,
    resolvedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusPill(label = "⚖  $openCount En arbitraje", bgColor = Color(0xFFEF4444))
        StatusPill(label = "🔍  $reviewCount En revisión", bgColor = Color(0xFFF59E0B))
        StatusPill(label = "✅  $resolvedCount Resueltas", bgColor = Color(0xFF10B981))
    }
}

@Composable
private fun StatusPill(label: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.15f))
            .border(1.dp, bgColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = bgColor,
        )
    }
}

@Composable
private fun FilterTabsRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) Primary else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Primary else BorderColor,
                        shape = RoundedCornerShape(50.dp),
                    )
                    .clickable { onTabSelected(index) }
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

@Composable
private fun DisputeCard(
    dispute: Dispute,
    onViewDetail: (String) -> Unit = {},
    onResolve: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (dispute.status) {
        "open" -> DangerColor
        "under_review" -> WarningColor
        else -> SuccessColor
    }
    val statusLabel = when (dispute.status) {
        "open" -> "ABIERTA"
        "under_review" -> "EN REVISIÓN"
        "resolved" -> "RESUELTA"
        else -> dispute.status.uppercase()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetail(dispute.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "#DSP-${dispute.id.takeLast(4).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Primary,
                    )
                    Text(
                        text = "#TX-${dispute.transaction_id.takeLast(4).uppercase()}",
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
                StatusBadge(label = statusLabel, color = statusColor)
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            // Motivo + descripción
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(dispute.reason, fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }

            dispute.description?.let {
                Text(text = it, fontSize = 12.sp, color = TextMuted, maxLines = 2)
            }

            // Fecha
            Text(
                text = "📅  ${dispute.created_at.take(10)}",
                fontSize = 11.sp,
                color = TextSubtle
            )

            // Botones resolución
            if (dispute.status == "open" || dispute.status == "under_review") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onResolve("favour_buyer") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, SuccessColor.copy(alpha = 0.5f))
                    ) {
                        Text("✓ Comprador", fontSize = 12.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onResolve("favour_vendor") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, WarningColor.copy(alpha = 0.5f))
                    ) {
                        Text("✓ Vendedor", fontSize = 12.sp, color = WarningColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
@Composable
private fun StatusBadge(label: String, color: Color) {
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

@Composable
private fun ComplaintAdminCard(
    complaint: com.example.p2p.data.remote.model.Complaint,
    onResolve: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var adminNote by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Text("Resolver Reclamo", fontWeight = FontWeight.Bold, color = TextMain)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Escribe una nota de resolución:", fontSize = 13.sp, color = TextMuted)
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        placeholder = { Text("Nota de resolución...", fontSize = 13.sp, color = TextSubtle) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminNote.isNotBlank()) {
                            onResolve(adminNote)
                            showDialog = false
                            adminNote = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Resolver", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    val statusColor = when (complaint.status) {
        "pending" -> WarningColor
        "under_review" -> InfoColor
        "resolved" -> SuccessColor
        else -> TextMuted
    }
    val statusLabel = when (complaint.status) {
        "pending" -> "PENDIENTE"
        "under_review" -> "EN REVISIÓN"
        "resolved" -> "RESUELTO"
        else -> complaint.status.uppercase()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "#RCL-${complaint.id.takeLast(4).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Primary,
                )
                StatusBadge(label = statusLabel, color = statusColor)
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Primary.copy(alpha = 0.10f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    com.example.p2p.data.remote.model.ComplaintType.label(complaint.type),
                    fontSize = 11.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(text = complaint.description, fontSize = 12.sp, color = TextMuted)

            complaint.admin_note?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessColor.copy(alpha = 0.08f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("✓", color = SuccessColor, fontSize = 12.sp)
                    Text(it, fontSize = 12.sp, color = SuccessColor)
                }
            }

            Text(text = "📅  ${complaint.created_at.take(10)}", fontSize = 11.sp, color = TextSubtle)

            if (complaint.status != "resolved" && complaint.status != "closed") {
                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, SuccessColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Resolver Reclamo", fontSize = 13.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

