package com.example.p2p.presentation.complaints

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Complaint
import com.example.p2p.data.remote.model.ComplaintStatus
import com.example.p2p.data.remote.model.ComplaintType
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(
    viewModel: ComplaintsViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ComplaintType.PLATFORM_ERROR) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reclamos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Nuevo Reclamo ─────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Nuevo Reclamo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    // Dropdown tipo de reclamo
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tipo de Reclamo", fontSize = 12.sp, color = TextMuted)
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedType.label, fontSize = 13.sp, color = TextMain)
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                ComplaintType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.label, fontSize = 13.sp) },
                                        onClick = {
                                            selectedType = type
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                "Describe tu reclamo con detalle...",
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

                    // Botón enviar
                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                Toast.makeText(context, "Por favor describe tu reclamo", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createComplaint(
                                    type = selectedType,
                                    description = description,
                                    onSuccess = {
                                        Toast.makeText(context, "Reclamo enviado con éxito", Toast.LENGTH_SHORT).show()
                                        description = ""
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                        enabled = !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Enviar Reclamo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ── Mis Reclamos ──────────────────────────────────────────────────
            Text(
                "Mis Reclamos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DangerColor)
                    }
                }
                uiState.complaints.isEmpty() -> {
                    Text(
                        "No tienes reclamos registrados.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
                else -> {
                    uiState.complaints.forEach { complaint ->
                        ComplaintItem(complaint = complaint)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ComplaintItem(complaint: Complaint) {
    val statusColor = when (complaint.status) {
        ComplaintStatus.IN_REVIEW.name  -> WarningColor
        ComplaintStatus.RESOLVED.name   -> SuccessColor
        ComplaintStatus.CLOSED.name     -> TextMuted
        else                            -> WarningColor
    }
    val statusLabel = ComplaintStatus.entries
        .firstOrNull { it.name == complaint.status }?.label ?: complaint.status

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("#${complaint.id}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                Text(
                    ComplaintType.entries.firstOrNull { it.name == complaint.type }?.label ?: complaint.type,
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(complaint.created_at.take(10), fontSize = 11.sp, color = TextMuted)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
    }
}