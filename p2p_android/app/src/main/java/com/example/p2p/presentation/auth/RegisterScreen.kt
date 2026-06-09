package com.example.p2p.presentation.auth

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.p2p.presentation.kyc.KycViewModel
import com.example.p2p.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel? = null,
    kycViewModel: KycViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = RegisterUiState())
        ?: remember { mutableStateOf(RegisterUiState()) }
    val kycState by kycViewModel?.state?.collectAsState(initial = com.example.p2p.presentation.kyc.KycState())
        ?: remember { mutableStateOf(com.example.p2p.presentation.kyc.KycState()) }

    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var contractAccepted by remember { mutableStateOf(false) }
    var signaturePaths by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var kycSubmitted by remember { mutableStateOf(false) }

    fun createUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("KYC_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { kycViewModel?.onDniFrontSelected(it) }
    }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { kycViewModel?.onDniBackSelected(it) }
    }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { kycViewModel?.onSelfieSelected(it) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_LONG).show()
    }

    fun launchCamera(launcher: androidx.activity.result.ActivityResultLauncher<Uri>) {
        val uri = createUri()
        tempUri = uri
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> launcher.launch(uri)
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val s = kycState
            if (s.dniFrontUri != null && s.dniBackUri != null && s.selfieUri != null) {
                kycSubmitted = true
                kycViewModel?.submitKyc(context)
            } else {
                onRegisterSuccess()
            }
        }
    }

    LaunchedEffect(kycState.isSuccess) {
        if (kycState.isSuccess) onRegisterSuccess()
    }

    LaunchedEffect(kycState.error) {
        if (kycState.error != null && kycSubmitted) {
            // Registration succeeded but KYC upload failed; proceed to market anyway
            onRegisterSuccess()
        }
    }

    val stepTitles = listOf("Datos", "KYC", "Contrato", "Contraseña")

    Box(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentStep == 1) onNavigateBack() else currentStep-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
                }
                Spacer(Modifier.width(8.dp))
                Text("Crear cuenta", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            }

            StepIndicator(currentStep = currentStep, totalSteps = 4, stepTitles = stepTitles)

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                when (currentStep) {
                    1 -> StepPersonalData(
                        fullName = fullName, email = email, dni = dni,
                        onFullNameChange = { fullName = it },
                        onEmailChange = { email = it },
                        onDniChange = { if (it.length <= 8) dni = it },
                        onNext = { if (fullName.isNotBlank() && email.isNotBlank() && dni.length == 8) currentStep = 2 }
                    )
                    2 -> StepKyc(
                        kycState = kycState,
                        onLaunchFront = { launchCamera(frontLauncher) },
                        onLaunchBack = { launchCamera(backLauncher) },
                        onLaunchSelfie = { launchCamera(selfieLauncher) },
                        onNextKycStep = { kycViewModel?.nextStep() },
                        onNext = { currentStep = 3 },
                        onSkip = { currentStep = 3 }
                    )
                    3 -> StepContract(
                        contractAccepted = contractAccepted,
                        onContractAcceptedChange = { contractAccepted = it },
                        signaturePaths = signaturePaths,
                        currentPath = currentPath,
                        onDrawStart = { offset -> currentPath = listOf(offset) },
                        onDrawMove = { offset -> currentPath = currentPath + offset },
                        onDrawEnd = {
                            if (currentPath.isNotEmpty()) {
                                signaturePaths = signaturePaths + listOf(currentPath)
                                currentPath = emptyList()
                            }
                        },
                        onClearSignature = { signaturePaths = emptyList(); currentPath = emptyList() },
                        onNext = { if (contractAccepted && signaturePaths.isNotEmpty()) currentStep = 4 }
                    )
                    4 -> StepPassword(
                        password = password, confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible, confirmPasswordVisible = confirmPasswordVisible,
                        termsAccepted = termsAccepted,
                        isLoading = uiState.isLoading || kycState.isLoading,
                        error = uiState.error ?: kycState.error,
                        onPasswordChange = { password = it },
                        onConfirmPasswordChange = { confirmPassword = it },
                        onPasswordVisibleChange = { passwordVisible = it },
                        onConfirmPasswordVisibleChange = { confirmPasswordVisible = it },
                        onTermsAcceptedChange = { termsAccepted = it },
                        onRegister = {
                            if (password == confirmPassword && termsAccepted) {
                                viewModel?.register(email, password, fullName, dni)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) { append("Inicia sesión") }
                    },
                    fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToLogin() }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int, stepTitles: List<String>) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isDone = step < currentStep
            val isCurrent = step == currentStep
            val color = when { isDone -> SuccessColor; isCurrent -> Primary; else -> BorderColor }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(when { isDone -> SuccessColor; isCurrent -> Primary; else -> SurfaceColor })
                        .border(2.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    else Text("$step", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else TextMuted)
                }
                Spacer(Modifier.height(4.dp))
                Text(stepTitles[index], fontSize = 10.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = color, textAlign = TextAlign.Center)
            }

            if (index < totalSteps - 1) {
                Box(modifier = Modifier.weight(0.5f).height(2.dp).background(if (index + 1 < currentStep) SuccessColor else BorderColor))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepPersonalData(
    fullName: String, email: String, dni: String,
    onFullNameChange: (String) -> Unit, onEmailChange: (String) -> Unit,
    onDniChange: (String) -> Unit, onNext: () -> Unit
) {
    val canContinue = fullName.isNotBlank() && email.isNotBlank() && "@" in email && dni.length == 8

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StepCardHeader(Icons.Default.Person, Primary, "Datos personales", "Paso 1 de 4")

            HorizontalDivider(color = BorderColor)

            FieldLabel("NOMBRE COMPLETO")
            OutlinedTextField(
                value = fullName, onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = TextMuted) },
                placeholder = { Text("Ej: Carlos Mendoza López", color = TextMuted, fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                colors = peruFieldColors(), singleLine = true
            )

            FieldLabel("CORREO ELECTRÓNICO")
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted) },
                placeholder = { Text("tu@email.com", color = TextMuted, fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = peruFieldColors(), singleLine = true
            )

            FieldLabel("NÚMERO DE DNI")
            OutlinedTextField(
                value = dni, onValueChange = onDniChange,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = TextMuted) },
                placeholder = { Text("12345678", color = TextMuted, fontSize = 13.sp) },
                supportingText = { Text("${dni.length}/8 dígitos", fontSize = 11.sp, color = if (dni.length == 8) SuccessColor else TextMuted) },
                trailingIcon = if (dni.length == 8) { { Icon(Icons.Default.CheckCircle, null, tint = SuccessColor) } } else null,
                isError = dni.isNotEmpty() && dni.length < 8,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                colors = peruFieldColors(), singleLine = true
            )

            Spacer(Modifier.height(4.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = canContinue) {
                Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StepKyc(
    kycState: com.example.p2p.presentation.kyc.KycState,
    onLaunchFront: () -> Unit,
    onLaunchBack: () -> Unit,
    onLaunchSelfie: () -> Unit,
    onNextKycStep: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val subStep = kycState.currentStep

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StepCardHeader(Icons.Default.CreditCard, WarningColor, "Verificación KYC", "Paso 2 de 4")

            HorizontalDivider(color = BorderColor)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                KycSubStepDot(1, subStep, "Frente")
                Box(modifier = Modifier.width(32.dp).height(2.dp).background(if (subStep > 1) SuccessColor else BorderColor))
                KycSubStepDot(2, subStep, "Dorso")
                Box(modifier = Modifier.width(32.dp).height(2.dp).background(if (subStep > 2) SuccessColor else BorderColor))
                KycSubStepDot(3, subStep, "Selfie")
            }

            when (subStep) {
                1 -> KycPhotoStep(
                    title = "DNI · Cara Frontal",
                    subtitle = "Captura la parte delantera de tu DNI",
                    icon = Icons.Default.CreditCard,
                    photoUri = kycState.dniFrontUri,
                    onLaunchCamera = onLaunchFront,
                    onNext = { if (kycState.dniFrontUri != null) onNextKycStep() },
                    canContinue = kycState.dniFrontUri != null
                )
                2 -> KycPhotoStep(
                    title = "DNI · Cara Posterior",
                    subtitle = "Captura la parte trasera de tu DNI",
                    icon = Icons.Default.Flip,
                    photoUri = kycState.dniBackUri,
                    onLaunchCamera = onLaunchBack,
                    onNext = { if (kycState.dniBackUri != null) onNextKycStep() },
                    canContinue = kycState.dniBackUri != null
                )
                3 -> KycPhotoStep(
                    title = "Selfie de Verificación",
                    subtitle = "Tómate una foto sosteniendo tu DNI junto a tu rostro",
                    icon = Icons.Default.CameraAlt,
                    photoUri = kycState.selfieUri,
                    onLaunchCamera = onLaunchSelfie,
                    onNext = onNext,
                    canContinue = kycState.selfieUri != null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(WarningColor.copy(alpha = 0.07f))
                    .border(1.dp, WarningColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, null, tint = WarningColor, modifier = Modifier.size(14.dp))
                Text("Tus documentos son procesados de forma segura y encriptada solo para verificar tu identidad.", fontSize = 11.sp, color = WarningColor)
            }

            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Completar después", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun KycPhotoStep(
    title: String, subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    photoUri: Uri?,
    onLaunchCamera: () -> Unit,
    onNext: () -> Unit,
    canContinue: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
        Text(subtitle, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)

        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, if (photoUri != null) SuccessColor else Primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .background(Primary.copy(alpha = 0.04f))
                .clickable { onLaunchCamera() },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(model = photoUri, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape).background(SuccessColor).padding(4.dp)) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, null, tint = Primary.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                    Text("Toca para capturar", fontSize = 12.sp, color = TextMuted)
                }
            }
        }

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = canContinue) {
            Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun KycSubStepDot(step: Int, currentStep: Int, label: String) {
    val isDone = currentStep > step
    val isActive = currentStep == step
    val color = when { isDone -> SuccessColor; isActive -> Primary; else -> BorderColor }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(when { isDone -> SuccessColor; isActive -> Primary; else -> SurfaceColor }).border(2.dp, color, CircleShape), contentAlignment = Alignment.Center) {
            if (isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            else Text("$step", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else TextMuted)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, color = color, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun StepContract(
    contractAccepted: Boolean, onContractAcceptedChange: (Boolean) -> Unit,
    signaturePaths: List<List<Offset>>, currentPath: List<Offset>,
    onDrawStart: (Offset) -> Unit, onDrawMove: (Offset) -> Unit, onDrawEnd: () -> Unit,
    onClearSignature: () -> Unit, onNext: () -> Unit
) {
    val hasSigned = signaturePaths.isNotEmpty()

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StepCardHeader(Icons.Default.Description, Primary, "Contrato de usuario", "Paso 3 de 4")
            HorizontalDivider(color = BorderColor)

            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .clip(RoundedCornerShape(12.dp)).background(BackgroundApp)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .verticalScroll(rememberScrollState()).padding(14.dp)
            ) {
                Text(CONTRACT_TEXT, fontSize = 11.sp, color = TextMuted, lineHeight = 17.sp)
            }

            FieldLabel("FIRMA ELECTRÓNICA")

            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color.White)
                    .border(1.5.dp, if (hasSigned) SuccessColor else BorderColor, RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                when {
                                    change.pressed && !change.previousPressed -> onDrawStart(change.position)
                                    change.pressed -> { change.consume(); onDrawMove(change.position) }
                                    !change.pressed && change.previousPressed -> onDrawEnd()
                                }
                            }
                        }
                    }
                ) {
                    (signaturePaths + if (currentPath.isNotEmpty()) listOf(currentPath) else emptyList()).forEach { points ->
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                            }
                            drawPath(path, color = Color(0xFF1A2332), style = Stroke(width = 3f))
                        }
                    }
                }
                if (!hasSigned && currentPath.isEmpty()) {
                    Text("Dibuja tu firma aquí", fontSize = 13.sp, color = Color.LightGray, modifier = Modifier.align(Alignment.Center))
                }
                if (hasSigned) {
                    TextButton(onClick = onClearSignature, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Refresh, null, tint = DangerColor, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Limpiar", fontSize = 11.sp, color = DangerColor)
                    }
                }
            }

            if (hasSigned) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                    Text("Firma registrada", fontSize = 11.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (contractAccepted) SuccessColor.copy(alpha = 0.07f) else BackgroundApp)
                    .border(1.dp, if (contractAccepted) SuccessColor.copy(alpha = 0.3f) else BorderColor, RoundedCornerShape(10.dp))
                    .clickable { onContractAcceptedChange(!contractAccepted) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(checked = contractAccepted, onCheckedChange = onContractAcceptedChange,
                    colors = CheckboxDefaults.colors(checkedColor = SuccessColor, uncheckedColor = BorderColor))
                Text("He leído y acepto el contrato de usuario de P2P Exchange.", fontSize = 12.sp,
                    color = if (contractAccepted) TextMain else TextMuted,
                    fontWeight = if (contractAccepted) FontWeight.SemiBold else FontWeight.Normal)
            }

            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = contractAccepted && hasSigned) {
                Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }

            if (!hasSigned || !contractAccepted) {
                Text(
                    buildString {
                        if (!hasSigned) append("• Dibuja tu firma\n")
                        if (!contractAccepted) append("• Acepta el contrato")
                    }.trim(),
                    fontSize = 11.sp, color = TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepPassword(
    password: String, confirmPassword: String,
    passwordVisible: Boolean, confirmPasswordVisible: Boolean,
    termsAccepted: Boolean, isLoading: Boolean, error: String?,
    onPasswordChange: (String) -> Unit, onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibleChange: (Boolean) -> Unit, onConfirmPasswordVisibleChange: (Boolean) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit, onRegister: () -> Unit
) {
    val passwordsMatch = confirmPassword.isEmpty() || password == confirmPassword
    val canRegister = password.length >= 8 && password == confirmPassword && termsAccepted

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StepCardHeader(Icons.Default.Lock, Primary, "Crea tu contraseña", "Paso 4 de 4")
            HorizontalDivider(color = BorderColor)

            FieldLabel("CONTRASEÑA")
            OutlinedTextField(
                value = password, onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted) },
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted)
                    }
                },
                placeholder = { Text("Mínimo 8 caracteres", color = TextMuted, fontSize = 13.sp) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                isError = password.isNotEmpty() && password.length < 8,
                supportingText = if (password.isNotEmpty() && password.length < 8) {
                    { Text("Mínimo 8 caracteres", fontSize = 11.sp, color = DangerColor) }
                } else null,
                colors = peruFieldColors(), singleLine = true
            )

            FieldLabel("CONFIRMAR CONTRASEÑA")
            OutlinedTextField(
                value = confirmPassword, onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextMuted) },
                trailingIcon = {
                    IconButton(onClick = { onConfirmPasswordVisibleChange(!confirmPasswordVisible) }) {
                        Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextMuted)
                    }
                },
                placeholder = { Text("Repite tu contraseña", color = TextMuted, fontSize = 13.sp) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = !passwordsMatch,
                supportingText = if (!passwordsMatch) { { Text("Las contraseñas no coinciden", fontSize = 11.sp, color = DangerColor) } } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = peruFieldColors(), singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onTermsAcceptedChange(!termsAccepted) }.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(checked = termsAccepted, onCheckedChange = onTermsAcceptedChange,
                    colors = CheckboxDefaults.colors(checkedColor = Primary, uncheckedColor = BorderColor))
                Text(buildAnnotatedString {
                    append("Acepto los ")
                    withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) { append("Términos y Condiciones") }
                }, fontSize = 13.sp, color = TextMain)
            }

            Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint, contentColor = Color(0xFF004D40)),
                enabled = canRegister && !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF004D40), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Crear cuenta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            error?.let { err ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(DangerColor.copy(alpha = 0.08f))
                        .border(1.dp, DangerColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = DangerColor, modifier = Modifier.size(16.dp))
                    Text(err, color = DangerColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StepCardHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String, subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
}

@Composable
private fun peruFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
    focusedContainerColor = SurfaceColor, unfocusedContainerColor = SurfaceColor,
    errorBorderColor = DangerColor, errorContainerColor = SurfaceColor
)

private const val CONTRACT_TEXT = """CONTRATO DE USUARIO — P2P EXCHANGE PLATFORM

Fecha de entrada en vigor: al momento del registro.

1. PARTES DEL CONTRATO
El presente contrato se celebra entre P2P Exchange Platform (en adelante "la Plataforma") y el usuario que procede a registrarse (en adelante "el Usuario").

2. OBJETO
La Plataforma ofrece un servicio de intermediación para el intercambio de divisas entre particulares (peer-to-peer), facilitando transacciones de compra y venta de moneda extranjera conforme a la normativa vigente.

3. OBLIGACIONES DEL USUARIO
- Proporcionar información veraz y actualizada durante el registro.
- Usar la plataforma únicamente para fines lícitos.
- No realizar operaciones que impliquen lavado de activos, financiamiento del terrorismo u otras actividades ilícitas.
- Mantener la confidencialidad de sus credenciales de acceso.
- Cumplir con los procesos de verificación KYC (Conoce a tu Cliente).

4. VERIFICACIÓN DE IDENTIDAD (KYC)
El Usuario acepta que la Plataforma podrá solicitar documentación adicional para verificar su identidad conforme a las regulaciones de la SBS (Superintendencia de Banca, Seguros y AFP del Perú).

5. OPERACIONES P2P
Las transacciones se realizan entre usuarios. La Plataforma actúa como facilitador y no es parte de la operación de cambio. El Usuario asume la responsabilidad de verificar la información de su contraparte.

6. COMISIONES Y TARIFAS
El uso de la plataforma puede estar sujeto a comisiones según la tabla de tarifas vigente, disponible en la sección de ayuda.

7. DISPUTAS
En caso de conflicto entre usuarios, la Plataforma podrá intervenir como mediador. Las decisiones del equipo de soporte son definitivas dentro del ámbito de la plataforma.

8. PRIVACIDAD DE DATOS
Los datos personales del Usuario serán tratados conforme a la Ley N° 29733 — Ley de Protección de Datos Personales del Perú y nuestra Política de Privacidad.

9. RESPONSABILIDAD LIMITADA
La Plataforma no garantiza la disponibilidad continua del servicio y no se responsabiliza por pérdidas derivadas de interrupciones técnicas, errores del usuario o fluctuaciones del mercado cambiario.

10. TERMINACIÓN
El Usuario puede cancelar su cuenta en cualquier momento. La Plataforma se reserva el derecho de suspender cuentas que incumplan estos términos.

11. LEY APLICABLE
Este contrato se rige por las leyes de la República del Perú. Cualquier disputa será sometida a los tribunales competentes de la ciudad de Lima.

Al firmar electrónicamente este contrato, el Usuario declara haber leído, comprendido y aceptado íntegramente los términos y condiciones aquí establecidos."""
