package com.example.p2p.presentation.transaction

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
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactionId: String? = null,
    currentUserId: String = "",
    viewModel: TransactionViewModel? = null,
    onNavigateToDispute: (String) -> Unit = {},
    onNavigateToReceipt: (String) -> Unit = {},
    onNavigateToRating: (String, Int) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel?.uiState?.collectAsState(initial = TransactionUiState()) ?: remember { mutableStateOf(TransactionUiState()) }
    var timeLeft by remember { mutableStateOf(15 * 60) }
    var isUploadingVoucher by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedStars by remember { mutableStateOf(0) }
    var previousStatus by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && transactionId != null) {
            selectedImageUri = uri
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
                        viewModel?.uploadVoucherFromBase64(transactionId, base64)
                        Toast.makeText(context, "Voucher subido. Esperando confirmación del vendedor.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No se pudo leer la imagen.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingVoucher = false
                }
            }
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel?.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(transactionId) {
        while (true) {
            delay(5000L)
            if (transactionId != null) {
                viewModel?.loadTransaction(transactionId)
            }
        }
    }

    LaunchedEffect(timeLeft, uiState.transaction?.status, isUploadingVoucher) {
        if (timeLeft > 0 && uiState.transaction?.status in listOf("pending", "accepted") && !isUploadingVoucher) {
            delay(1000L)
            timeLeft--
        }
    }

    LaunchedEffect(uiState.transaction?.status, currentUserId) {
        val t = uiState.transaction
        val newStatus = t?.status ?: return@LaunchedEffect

        if (newStatus == "completed" &&
            previousStatus in listOf("voucher_uploaded", "pending", "accepted") &&
            currentUserId.isNotBlank() &&
            currentUserId == t.buyer_id
        ) {
            delay(1500L)
            showRatingDialog = true
        }
        previousStatus = newStatus
    }

    val txn = uiState.transaction
    val vendorName = txn?.vendor_name ?: "Vendedor"
    val vendorPayment = txn?.vendor_payment_account ?: "BCP"
    val vendorBankParts = vendorPayment.split(" · ")
    val vendorBank = vendorBankParts[0]
    val vendorAccountNumber = vendorBankParts.getOrNull(1)
    val statusText = when (txn?.status) {
        "pending" -> "ORDEN P2P EN CURSO"
        "accepted" -> "VENDEDOR ACEPTÓ TU ORDEN"
        "voucher_uploaded" -> "VERIFICANDO PAGO"
        "completed" -> "COMPLETADA"
        "cancelled" -> "CANCELADA"
        "disputed" -> "EN DISPUTA"
        else -> "ORDEN P2P EN CURSO"
    }

    val amountTo = txn?.amount_to ?: 0.0
    val amountFrom = txn?.amount_from ?: 0.0
    val currency = "USD"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transacción P2P",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = TextMain
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        val isTimerActive = txn?.status in listOf("pending", "accepted") && timeLeft > 0
        val isExpired = txn?.status in listOf("pending", "accepted") && timeLeft == 0
        val showStickyTimer = isTimerActive && scrollState.value > 300

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

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
                            .background(PrimaryMint.copy(alpha = 0.18f))
                            .border(1.dp, PrimaryMint.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMint,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (txn?.status == "voucher_uploaded" || txn?.status == "completed") {
                        Text(
                            text = "VOUCHER SUBIDO",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMint,
                            letterSpacing = 1.sp
                        )
                    } else {
                        val minutes = timeLeft / 60
                        val seconds = timeLeft % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
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

            val currentStep = when (txn?.status) {
                "pending"          -> if (isUploadingVoucher) 1 else 0
                "accepted"         -> 0
                "voucher_uploaded" -> 1
                "completed"        -> 3
                "disputed"         -> 2
                else               -> 0
            }

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
                    val steps = listOf("Pagar", "Voucher", "Confirmar", "Liberado")
                    steps.forEachIndexed { index, label ->

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= currentStep) Primary
                                    else Color.Transparent
                                )
                                .then(
                                    if (index > currentStep) Modifier.border(2.dp, Primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
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
                    val steps = listOf("Pagar", "Voucher", "Confirmar", "Liberado")
                    steps.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (index <= currentStep) FontWeight.Bold else FontWeight.Normal,
                            color = if (index <= currentStep) Primary else TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.08f))
                    .border(1.dp, Primary.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CUENTA RECEPTORA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = vendorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = vendorBank,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                if (vendorAccountNumber != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("N° cuenta:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = vendorAccountNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                    }
                }
            }

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
                    Column {
                        Text("Pagas exactamente:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "S/ ${String.format("%.2f", amountTo)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Recibirás:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "${String.format("%.2f", amountFrom)} $currency",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
                Text(
                    text = "Cuenta destino: $vendorBank",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            if (txn?.status == "accepted") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SuccessColor.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessColor)
                        Column {
                            Text("¡El vendedor aceptó tu orden!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextMain)
                            Text("Realiza el pago a la cuenta indicada y sube tu comprobante.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            if (txn?.status == "pending" && !isUploadingVoucher) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.07f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Primary)
                        Column {
                            Text("Esperando al vendedor...", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextMain)
                            Text("El vendedor fue notificado. Aceptará tu orden en breve.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            if (txn?.status == "pending" || txn?.status == "accepted") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor)
                        .border(
                            width = 1.5.dp,
                            color = when {
                                isUploadingVoucher -> Primary
                                selectedBitmap != null -> SuccessColor
                                else -> BorderColor
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isUploadingVoucher || uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Primary, strokeWidth = 3.dp)
                                Text("Subiendo voucher...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            }
                        }
                    } else {

                        if (selectedBitmap != null) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Comprobante de pago",
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
                                    Text("✓ Cargado", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (selectedFileName.isNotEmpty()) selectedFileName else "comprobante_pago.jpg",
                                    fontSize = 12.sp,
                                    color = SuccessColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedBitmap != null) SuccessColor.copy(alpha = 0.06f) else Primary.copy(alpha = 0.06f))
                                .border(1.dp, if (selectedBitmap != null) SuccessColor.copy(alpha = 0.3f) else Primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable { imagePicker.launch("image/*") }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedBitmap != null) Icons.Filled.PhotoLibrary else Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = if (selectedBitmap != null) SuccessColor else Primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = if (selectedBitmap != null) "Cambiar imagen" else "Subir Comprobante de Pago",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedBitmap != null) SuccessColor else TextMain
                                    )
                                    Text(
                                        text = "Selecciona desde galería",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (txn?.status == "voucher_uploaded") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningColor.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningColor.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = WarningColor)
                            Column {
                                Text("Esperando confirmación del vendedor...", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMain)
                                Text("El vendedor está verificando tu pago en su cuenta bancaria. Recibirás una notificación cuando libere los fondos.", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateToDispute(transactionId ?: "") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Abrir Disputa (vendedor no responde)", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            if (txn?.status == "completed") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SuccessColor.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 40.sp,
                            color = SuccessColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¡Cambio de divisas realizado con éxito!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextMain,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Los fondos han sido liberados correctamente.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Button(
                    onClick = { onNavigateToReceipt(transactionId ?: "") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Comprobante Exitoso", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (txn?.status == "pending" || txn?.status == "accepted") {
                OutlinedButton(
                    onClick = {
                        if (transactionId != null) {
                            viewModel?.updateStatus(transactionId, "cancelled")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DangerColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor)
                ) {
                    Text(
                        text = "Cancelar Transacción",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            if (isExpired) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DangerColor.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = DangerColor)
                        Column {
                            Text("Tiempo expirado", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DangerColor)
                            Text("El tiempo de la operación venció. Puedes cancelarla.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showStickyTimer) {
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            val timerColor = if (timeLeft < 120) DangerColor else WarningColor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(timerColor.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tiempo restante: ${String.format("%02d:%02d", minutes, seconds)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        }
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(20.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 40.sp
                    )
                    Text(
                        text = "¡Operación completada!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "¿Cómo fue tu experiencia con el vendedor?",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { star ->
                            Text(
                                text = if (star <= selectedStars) "★" else "☆",
                                fontSize = 38.sp,
                                color = if (star <= selectedStars) Color(0xFFF59E0B) else TextMuted,
                                modifier = Modifier
                                    .clickable { selectedStars = star }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                    if (selectedStars > 0) {
                        Text(
                            text = when (selectedStars) {
                                1 -> "Muy mala experiencia"
                                2 -> "Mala experiencia"
                                3 -> "Experiencia regular"
                                4 -> "Buena experiencia"
                                else -> "¡Excelente experiencia!"
                            },
                            fontSize = 12.sp,
                            color = if (selectedStars >= 4) SuccessColor else if (selectedStars == 3) Color(0xFFF59E0B) else DangerColor,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRatingDialog = false
                        onNavigateToRating(transactionId ?: "", selectedStars.coerceAtLeast(1))
                    },
                    enabled = selectedStars > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enviar calificación", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Omitir por ahora", color = TextMuted)
                }
            }
        )
    }
}
