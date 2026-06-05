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
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.PrimaryMint
import com.example.p2p.ui.theme.SuccessColor
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
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

            // Section title
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

            // Dispute cards
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
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A2332), Color(0xFF0F172A)),
                ),
            )
            .padding(20.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(PrimaryMint)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "CONTROL DE OPERACIONES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.5.sp,
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "ADM · Perú Exchange",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdminStat(value = "S/ ${String.format(Locale.getDefault(), "%.1fK", volume / 1000)}", label = "Volumen", valueColor = Color.White)
                StatDivider()
                AdminStat(value = disputesCount.toString(), label = "Disputas", valueColor = DangerColor)
                StatDivider()
                AdminStat(value = usersCount.toString(), label = "Usuarios", valueColor = Color.White)
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
        StatusPill(label = "⚖ $openCount En arbitraje", bgColor = DangerColor)
        StatusPill(label = "🔍 $reviewCount En revisión", bgColor = WarningColor)
        StatusPill(label = "✅ $resolvedCount Resueltas", bgColor = SuccessColor)
    }
}

@Composable
private fun StatusPill(label: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
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
    Card(
        modifier = modifier.fillMaxWidth().clickable { onViewDetail(dispute.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = dispute.transaction_id.take(8).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DangerColor,
                )
                StatusBadge(
                    label = dispute.status.uppercase(),
                    color = when (dispute.status) {
                        "open" -> DangerColor
                        "under_review" -> WarningColor
                        else -> SuccessColor
                    }
                )
            }

            Text(text = "Motivo: ${dispute.reason}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            dispute.description?.let {
                Text(text = it, fontSize = 12.sp, color = TextMuted)
            }

            Text(
                text = dispute.created_at.take(10),
                fontSize = 12.sp,
                color = TextMuted
            )

            if (dispute.status == "open" || dispute.status == "under_review") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                ) {
                    Button(
                        onClick = { onResolve("favour_buyer") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).weight(1f),
                    ) {
                        Text("✓ Comprador", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = { onResolve("favour_vendor") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).weight(1f),
                    ) {
                        Text("✓ Vendedor", fontSize = 11.sp, color = Color.White)
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
            title = { Text("Resolver Reclamo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Escribe una nota de resolución para el usuario:", fontSize = 13.sp, color = TextMuted)
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        placeholder = { Text("Nota de resolución...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4,
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
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Text("Resolver", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "#RCL-${complaint.id.takeLast(4).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Primary,
                )
                StatusBadge(
                    label = when (complaint.status) {
                        "pending"      -> "PENDIENTE"
                        "under_review" -> "EN REVISIÓN"
                        "resolved"     -> "RESUELTO"
                        else           -> complaint.status.uppercase()
                    },
                    color = when (complaint.status) {
                        "pending"      -> WarningColor
                        "under_review" -> Primary
                        "resolved"     -> SuccessColor
                        else           -> TextMuted
                    }
                )
            }

            Text(
                text = "Tipo: ${com.example.p2p.data.remote.model.ComplaintType.label(complaint.type)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )

            Text(
                text = complaint.description,
                fontSize = 12.sp,
                color = TextMuted
            )

            complaint.admin_note?.let {
                Text(
                    text = "Nota admin: $it",
                    fontSize = 12.sp,
                    color = SuccessColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = complaint.created_at.take(10),
                fontSize = 12.sp,
                color = TextMuted
            )

            if (complaint.status != "resolved" && complaint.status != "closed") {
                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                ) {
                    Text("Resolver Reclamo", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
