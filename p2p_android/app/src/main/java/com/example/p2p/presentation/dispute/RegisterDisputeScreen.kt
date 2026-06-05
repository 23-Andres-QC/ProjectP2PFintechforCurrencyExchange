package com.example.p2p.presentation.dispute
import com.example.p2p.data.remote.model.DisputeReason
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDisputeScreen(
    transactionId: String? = null,
    viewModel: DisputesViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(DisputesUiState()) }

    val reasons = listOf(
        "El vendedor no liberó los fondos" to DisputeReason.PAYMENT_NOT_RECEIVED,
        "El comprador no realizó el pago"  to DisputeReason.PAYMENT_NOT_RECEIVED,
        "El voucher no corresponde al monto" to DisputeReason.VOUCHER_FAKE,
        "Fondos enviados al banco incorrecto" to DisputeReason.WRONG_AMOUNT,
        "Otro motivo" to DisputeReason.OTHER
    )

    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var reasonExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar Disputa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextMain)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Info banner ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DangerColor.copy(alpha = 0.1f))
                    .border(1.dp, DangerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = DangerColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Las disputas son revisadas en 5 días hábiles.",
                    fontSize = 13.sp,
                    color = DangerColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Transaction selection ─────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Transacción Seleccionada",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                // TX selected
                TransactionCard(
                    id = transactionId ?: "No seleccionada",
                    amount = "Monto en disputa",
                    isSelected = true
                )
            }

            // ── Dispute reason ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Motivo de la Disputa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                // Dropdown reason selector
                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = it }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedReason.first, fontSize = 13.sp, color = TextMain, modifier = Modifier.weight(1f))
                            Icon(
                                if (reasonExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = reasonExpanded,
                        onDismissRequest = { reasonExpanded = false }
                    ) {
                        reasons.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.first, fontSize = 13.sp) },
                                onClick = {
                                    selectedReason = option
                                    reasonExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            "Describe con detalle qué ocurrió...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerColor,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    )
                )
            }

            // ── Evidence upload ───────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Adjuntar Evidencia",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = BorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Upload,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            "Evidencia adjunta (Simulada)",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // ── Submit button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    if (transactionId != null) {
                        viewModel?.createDispute(
                            transactionId = transactionId,
                            reason = selectedReason.second,   // ← envía la KEY, no el label
                            description = description,
                            onSuccess = {
                                Toast.makeText(context, "Disputa registrada con éxito", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Debe seleccionar una transacción válida", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Enviar Disputa",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TransactionCard(id: String, amount: String, isSelected: Boolean) {
    val borderColor = if (isSelected) Primary else BorderColor
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) Primary.copy(alpha = 0.05f) else SurfaceColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                id,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Primary else TextMain
            )
            Text(amount, fontSize = 13.sp, color = TextMuted)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .border(1.5.dp, BorderColor, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
