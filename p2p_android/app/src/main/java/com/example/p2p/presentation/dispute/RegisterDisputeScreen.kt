package com.example.p2p.presentation.dispute
import com.example.p2p.data.remote.model.DisputeReason
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.example.p2p.core.util.compressImageFromUri
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDisputeScreen(
    transactionId: String? = null,
    viewModel: DisputesViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
    var evidenceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var evidenceBase64 by remember { mutableStateOf<String?>(null) }
    var isProcessingEvidence by remember { mutableStateOf(false) }

    val evidencePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessingEvidence = true
                try {
                    val bytes = withContext(Dispatchers.IO) { compressImageFromUri(context, uri) }
                    if (bytes != null) {
                        evidenceBitmap?.recycle()
                        evidenceBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        evidenceBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    } else {
                        Toast.makeText(context, "No se pudo leer la imagen.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isProcessingEvidence = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { evidenceBitmap?.recycle() }
    }

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
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningColor.copy(alpha = 0.1f))
                    .border(1.dp, WarningColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = WarningColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Las disputas son revisadas en 5 días hábiles.",
                    fontSize = 13.sp,
                    color = TextMain,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "TRANSACCIÓN SELECCIONADA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )

                TransactionCard(
                    id = transactionId?.take(8)?.uppercase() ?: "No seleccionada",
                    amount = "Monto en disputa",
                    isSelected = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "MOTIVO DE LA DISPUTA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )

                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = it }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DangerColor.copy(alpha = 0.04f))
                            .border(1.dp, if (reasonExpanded) DangerColor else BorderColor, RoundedCornerShape(10.dp))
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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "ADJUNTAR EVIDENCIA (OPCIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (evidenceBitmap != null) 160.dp else 90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (evidenceBitmap != null) 1.dp else 2.dp,
                            color = if (evidenceBitmap != null) SuccessColor.copy(alpha = 0.4f) else BorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(if (evidenceBitmap != null) SuccessColor.copy(alpha = 0.06f) else Color(0xFFF8FAFC))
                        .clickable(enabled = !isProcessingEvidence) { evidencePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isProcessingEvidence -> CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                        evidenceBitmap != null -> {
                            Image(
                                bitmap = evidenceBitmap!!.asImageBitmap(),
                                contentDescription = "Evidencia adjunta",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Toca para cambiar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(22.dp))
                                Text("Toca para adjuntar una captura o foto", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (transactionId != null) {
                        viewModel?.createDispute(
                            transactionId = transactionId,
                            reason = selectedReason.second,
                            description = description,
                            evidenceBase64 = evidenceBase64,
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
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(14.dp),
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
}
