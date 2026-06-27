package com.example.p2p.presentation.vendor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var confirmingTransaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPendingTransactions()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            viewModel.loadPendingTransactions()
        }
    }

    // Show seller confirm screen when "Confirmar Pago y Liberar" is clicked
    if (confirmingTransaction != null) {
        VendorConfirmScreen(
            transaction = confirmingTransaction!!,
            viewModel = viewModel,
            onConfirm = { onDone ->
                viewModel.confirmTransaction(
                    confirmingTransaction!!.id,
                    onSuccess = {
                        confirmingTransaction = null
                        Toast.makeText(context, "¡Fondos liberados con éxito!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onError = { err ->
                        onDone()
                        Toast.makeText(context, "Error al confirmar: $err", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onBack = { confirmingTransaction = null }
        )
        return
    }

    confirmAcceptTxnId?.let { txnId ->
        AlertDialog(
            onDismissRequest = { confirmAcceptTxnId = null },
            title = { Text("¿Estás seguro?", fontWeight = FontWeight.Bold, color = TextMain) },
            text = { Text("¿Deseas aceptar esta orden de compra? El comprador será notificado para realizar el pago.", color = TextMuted, fontSize = 14.sp) },
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
                ) { Text("Sí, aceptar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmAcceptTxnId = null }) { Text("No", color = TextMuted) }
            },
            containerColor = SurfaceColor
        )
    }

    confirmCancelTxnId?.let { txnId ->
        AlertDialog(
            onDismissRequest = { confirmCancelTxnId = null },
            title = { Text("¿Estás seguro?", fontWeight = FontWeight.Bold, color = TextMain) },
            text = { Text("¿Deseas rechazar esta orden de compra? La operación será cancelada.", color = TextMuted, fontSize = 14.sp) },
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
                ) { Text("Sí, rechazar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancelTxnId = null }) { Text("No", color = TextMuted) }
            },
            containerColor = SurfaceColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendientes", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextMain)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ÓRDENES ACTIVAS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = WarningColor, letterSpacing = 1.sp)
                        Text("Órdenes Pendientes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                        Text("Acepta, cancela o confirma los pagos activos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp))
                    }
                    Box(
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(WarningColor.copy(alpha = 0.2f)),
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
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Inbox, contentDescription = null, tint = BorderColor, modifier = Modifier.size(48.dp))
                        Text("Sin órdenes activas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)
                        Text("Las órdenes pendientes de acción aparecerán aquí. El historial completo está en tu perfil.", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pendingTransactions) { txn ->
                        VendorTransactionCard(
                            transaction = txn,
                            onAccept = { confirmAcceptTxnId = txn.id },
                            onConfirm = { confirmingTransaction = txn },
                            onCancel = { confirmCancelTxnId = txn.id }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VendorConfirmScreen(
    transaction: Transaction,
    viewModel: TransactionViewModel,
    onConfirm: (onDone: () -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var timeLeft by remember { mutableStateOf(15 * 60) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploadingVoucher by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    selectedFileName = cursor.getString(nameIndex)
                }
            }
            scope.launch {
                isUploadingVoucher = true
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        selectedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                        val success = viewModel.uploadVoucherFromBase64Async(transaction.id, base64)
                        if (success) {
                            Toast.makeText(context, "Boleta subida correctamente.", Toast.LENGTH_LONG).show()
                        } else {
                            selectedBitmap = null
                            selectedFileName = ""
                            Toast.makeText(context, "Error al subir la boleta. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "No se pudo leer la imagen.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    selectedBitmap = null
                    selectedFileName = ""
                    Toast.makeText(context, "Error al procesar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingVoucher = false
                }
            }
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val boletaReady = selectedBitmap != null
    val currentStep = if (boletaReady) 3 else 2
    val steps = listOf("Inicio", "Pagar", "Boleta", "Confirmar", "Liberado")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transacción P2P",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = TextMain
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Header con cuenta regresiva ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1A2332), Color(0xFF0F172A))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (boletaReady) SuccessColor.copy(alpha = 0.18f)
                                else WarningColor.copy(alpha = 0.18f)
                            )
                            .border(
                                1.dp,
                                if (boletaReady) SuccessColor.copy(alpha = 0.5f)
                                else WarningColor.copy(alpha = 0.5f),
                                RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (boletaReady) "BOLETA LISTA" else "VERIFICANDO PAGO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (boletaReady) SuccessColor else WarningColor,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (boletaReady) {
                        Text(
                            text = "BOLETA SUBIDA",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMint,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 120) DangerColor else Color.White,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Capital en custodia · Operación segura",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Indicador de pasos ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= currentStep) Primary else Color.Transparent
                                )
                                .then(
                                    if (index > currentStep) Modifier.border(2.dp, Primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index <= currentStep) Color.White else Primary
                            )
                        }
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(
                                        if (index < currentStep) Primary.copy(alpha = 0.5f)
                                        else BorderColor
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    steps.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (index <= currentStep) FontWeight.Bold else FontWeight.Normal,
                            color = if (index <= currentStep) Primary else TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Resumen de transacción ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Comprador", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = transaction.buyer_name ?: transaction.buyer_id.take(8).uppercase(),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain
                    )
                }
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Recibes:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "S/ ${String.format("%.2f", transaction.amount_to)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tasa:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "${transaction.exchange_rate}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // ── Sección de subir boleta del vendedor ─────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = WarningColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Tu boleta de transferencia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextMain
                    )
                    if (boletaReady) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Lista", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceColor)
                        .border(
                            width = 1.5.dp,
                            color = when {
                                isUploadingVoucher -> WarningColor
                                boletaReady -> SuccessColor
                                else -> WarningColor.copy(alpha = 0.35f)
                            },
                            shape = RoundedCornerShape(14.dp)

                            onCancel = { confirmCancelTxnId = txn.id },
                            onUploadVendorVoucher = { base64: String, onSuccess: () -> Unit, onError: (String) -> Unit ->
                                viewModel.uploadVendorVoucherFromBase64(txn.id, base64, onSuccess, onError)
                            },
                            onConfirm = { vendorVoucherReady: Boolean ->
                                if (vendorVoucherReady) {
                                    viewModel.confirmTransaction(txn.id,
                                        onSuccess = { Toast.makeText(context, "¡Fondos liberados con éxito!", Toast.LENGTH_SHORT).show() },
                                        onError = { err -> Toast.makeText(context, "Error al confirmar: $err", Toast.LENGTH_LONG).show() }
                                    )
                                }
                            }
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isUploadingVoucher) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = WarningColor,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    "Subiendo boleta...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                            }
                        }
                    } else {
                        if (boletaReady) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Boleta de transferencia",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(SuccessColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("✓ Cargada", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessColor.copy(alpha = 0.08f))
                                    .border(1.dp, SuccessColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (selectedFileName.isNotEmpty()) selectedFileName else "boleta_transferencia.jpg",
                                    fontSize = 12.sp,
                                    color = SuccessColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (boletaReady) SuccessColor.copy(alpha = 0.08f)
                                    else WarningColor.copy(alpha = 0.07f)
                                )
                                .border(
                                    1.5.dp,
                                    if (boletaReady) SuccessColor.copy(alpha = 0.4f)
                                    else WarningColor.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !isUploadingVoucher) { imagePicker.launch("image/*") }
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (boletaReady) Icons.Filled.PhotoLibrary else Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = if (boletaReady) SuccessColor else WarningColor,
                                    modifier = Modifier.size(30.dp)
                                )
                                Column {
                                    Text(
                                        text = if (boletaReady) "Cambiar boleta" else "Subir Boleta de Transferencia",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (boletaReady) SuccessColor else WarningColor
                                    )
                                    Text(
                                        text = if (boletaReady) "Toca para reemplazar" else "Toca aquí · Galería o cámara",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                if (!boletaReady) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(WarningColor.copy(alpha = 0.08f))
                            .border(1.dp, WarningColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = WarningColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Sube tu boleta de transferencia antes de liberar los fondos.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // ── Botón Liberar Fondos ─────────────────────────────────────────────
            Button(
                onClick = {
                    isConfirming = true
                    onConfirm { isConfirming = false }
                },
                enabled = boletaReady && !isConfirming && !isUploadingVoucher,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessColor,
                    disabledContainerColor = SuccessColor.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Liberando fondos...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Liberar Fondos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (!boletaReady) {
                Text(
                    text = "Sube tu boleta para habilitar el botón",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VendorTransactionCard(
    transaction: Transaction,
    onAccept: () -> Unit,
    onCancel: () -> Unit = {},
    onUploadVendorVoucher: (String, () -> Unit, (String) -> Unit) -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNewOrder = transaction.status == "pending"
    val isAccepted = transaction.status == "accepted"
    val isVoucherUploaded = transaction.status == "voucher_uploaded"
    val isCompleted = transaction.status == "completed"

    var vendorBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var vendorVoucherReady by remember { mutableStateOf(false) }
    var isUploadingVendorVoucher by remember { mutableStateOf(false) }

    val vendorImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isUploadingVendorVoucher = true
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        vendorBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                        onUploadVendorVoucher(
                            base64,
                            {
                                vendorVoucherReady = true
                                isUploadingVendorVoucher = false
                                Toast.makeText(context, "Comprobante subido. Ya puedes liberar.", Toast.LENGTH_LONG).show()
                            },
                            { err ->
                                isUploadingVendorVoucher = false
                                vendorBitmap = null
                                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        isUploadingVendorVoucher = false
                        Toast.makeText(context, "No se pudo leer la imagen.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    isUploadingVendorVoucher = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val statusLabel = when (transaction.status) {
        "pending"          -> "NUEVA ORDEN DE COMPRA"
        "accepted"         -> "ORDEN ACEPTADA · ESPERANDO PAGO"
        "voucher_uploaded" -> "PAGO RECIBIDO · SUBE TU COMPROBANTE"
        "completed"        -> "FONDOS LIBERADOS · CERRAR"
        else               -> transaction.status.uppercase()
    }
    val statusColor = when (transaction.status) {
        "pending"          -> Primary
        "accepted"         -> SuccessColor
        "voucher_uploaded" -> WarningColor
        "completed"        -> SuccessColor
        else               -> TextMuted
    }
    val cardBorder = when (transaction.status) {
        "pending"          -> Primary.copy(alpha = 0.4f)
        "accepted"         -> SuccessColor.copy(alpha = 0.3f)
        "voucher_uploaded" -> WarningColor.copy(alpha = 0.3f)
        "completed"        -> SuccessColor.copy(alpha = 0.4f)
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

            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor, letterSpacing = 0.8.sp)
                    Text(transaction.id.take(8).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain, modifier = Modifier.padding(top = 2.dp))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(transaction.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            // Banner nueva orden
            if (isNewOrder) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.07f))
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text("Un comprador quiere comprarte divisas. Acepta para confirmar la operación.", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
                }
            }

            // Info comprador y monto
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BackgroundApp).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Comprador", fontSize = 12.sp, color = TextMuted)
                    Text(transaction.buyer_name ?: transaction.buyer_id.take(8).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Monto", fontSize = 12.sp, color = TextMuted)
                    Text("S/ ${String.format("%.2f", transaction.amount_to)} · tasa ${transaction.exchange_rate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
                }
            }

            // PENDING: aceptar / rechazar
            if (isNewOrder) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Aceptar orden de compra", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor), border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rechazar orden", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ACCEPTED: esperando pago
            if (isAccepted) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(SuccessColor.copy(alpha = 0.07f))
                            .border(1.dp, SuccessColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                        Text("Orden aceptada. Esperando que el comprador realice el pago y suba su comprobante.", fontSize = 11.sp, color = SuccessColor, fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(onClick = onCancel, colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor), border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar operación", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // VOUCHER_UPLOADED: vendedor sube su comprobante antes de liberar
            if (isVoucherUploaded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(WarningColor.copy(alpha = 0.08f))
                            .border(1.dp, WarningColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = WarningColor, modifier = Modifier.size(16.dp))
                        Column {
                            Text("El comprador ya realizó el pago.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            Text("Verifica tu cuenta y sube tu comprobante de transferencia de USD antes de liberar.", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                        Text("TU COMPROBANTE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                    }

                    // Upload zone
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceColor)
                            .border(
                                1.5.dp,
                                when {
                                    isUploadingVendorVoucher -> WarningColor
                                    vendorVoucherReady -> SuccessColor
                                    else -> BorderColor
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isUploadingVendorVoucher) {
                            Box(modifier = Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(26.dp), color = WarningColor, strokeWidth = 3.dp)
                                    Text("Subiendo comprobante...", fontSize = 12.sp, color = TextMain)
                                }
                            }
                        } else {
                            if (vendorBitmap != null) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Image(
                                        bitmap = vendorBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (vendorVoucherReady) {
                                        Box(
                                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                                                .clip(RoundedCornerShape(50.dp)).background(SuccessColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("✓ Subido", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(if (vendorVoucherReady) SuccessColor.copy(alpha = 0.06f) else WarningColor.copy(alpha = 0.06f))
                                    .border(1.dp, if (vendorVoucherReady) SuccessColor.copy(alpha = 0.3f) else WarningColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { vendorImagePicker.launch("image/*") }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = if (vendorVoucherReady) Icons.Filled.PhotoLibrary else Icons.Filled.CloudUpload,
                                        contentDescription = null,
                                        tint = if (vendorVoucherReady) SuccessColor else WarningColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (vendorVoucherReady) "Cambiar comprobante" else "Subir mi comprobante de transferencia",
                                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                            color = if (vendorVoucherReady) SuccessColor else TextMain
                                        )
                                        Text(
                                            text = if (vendorVoucherReady) "Listo · ya puedes liberar" else "Foto de tu transferencia de USD al comprador",
                                            fontSize = 10.sp, color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onConfirm(vendorVoucherReady) },
                        enabled = vendorVoucherReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessColor,
                            disabledContainerColor = SuccessColor.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (vendorVoucherReady) "Confirmar y Liberar Fondos" else "Sube tu comprobante para liberar",
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // COMPLETED
            if (isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(SuccessColor.copy(alpha = 0.07f))
                        .border(1.dp, SuccessColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                    Text("Fondos liberados. El comprador está cerrando la operación.", fontSize = 11.sp, color = SuccessColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}