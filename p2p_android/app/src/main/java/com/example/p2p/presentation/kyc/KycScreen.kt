package com.example.p2p.presentation.kyc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.core.content.FileProvider
import android.net.Uri
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    onNavigateBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: KycViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }

    fun createUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("KYC_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { viewModel.onDniFrontSelected(it) }
    }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { viewModel.onDniBackSelected(it) }
    }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempUri?.let { viewModel.onSelfieSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Se requiere permiso de cámara para continuar", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCameraWithPermission(launcher: androidx.activity.result.ActivityResultLauncher<Uri>, uri: Uri) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                launcher.launch(uri)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val currentStep = state.currentStep
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundApp
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextMain
                        )
                    }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Verificación KYC",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KycStepDot(stepIndex = 1, currentStep = currentStep, label = "Frente")
                KycStepLine(isCompleted = currentStep > 1)
                KycStepDot(stepIndex = 2, currentStep = currentStep, label = "Dorso")
                KycStepLine(isCompleted = currentStep > 2)
                KycStepDot(stepIndex = 3, currentStep = currentStep, label = "Selfie")
            }

            KycStepCard(
                isActive = currentStep == 1,
                isCompleted = currentStep > 1,
                stepNumber = 1
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "DNI · Cara Frontal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Toca para capturar o subir la parte\ndelantera de tu DNI",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 2.dp,
                                color = if (state.dniFrontUri != null) SuccessColor else Primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(Primary.copy(alpha = 0.04f))
                            .clickable {
                                val uri = createUri()
                                tempUri = uri
                                launchCameraWithPermission(frontLauncher, uri)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.dniFrontUri != null) {
                            AsyncImage(
                                model = state.dniFrontUri,
                                contentDescription = "DNI Frontal",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0F4FF))
                                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFD32F2F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("REPÚBLICA DEL PERÚ", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.Center).size(32.dp),
                                    tint = Primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = state.dniFrontUri != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(
                            "Continuar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            KycStepCard(
                isActive = currentStep == 2,
                isCompleted = currentStep > 2,
                stepNumber = 2,
                modifier = Modifier.alpha(if (currentStep >= 2) 1f else 0.45f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (currentStep >= 2) Primary.copy(alpha = 0.12f)
                                else BorderColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            tint = if (currentStep >= 2) Primary else TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "DNI · Cara Posterior",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStep >= 2) TextMain else TextMuted
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Toca para capturar o subir la parte\ntrasera de tu DNI",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    if (currentStep == 2) {
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (state.dniBackUri != null) SuccessColor else Primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(Primary.copy(alpha = 0.04f))
                                .clickable {
                                    val uri = createUri()
                                    tempUri = uri
                                    launchCameraWithPermission(backLauncher, uri)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.dniBackUri != null) {
                                AsyncImage(
                                    model = state.dniBackUri,
                                    contentDescription = "DNI Posterior",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = null,
                                        tint = Primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Toca para capturar el dorso",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = state.dniBackUri != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(
                                "Continuar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            KycStepCard(
                isActive = currentStep == 3,
                isCompleted = false,
                stepNumber = 3,
                modifier = Modifier.alpha(if (currentStep >= 3) 1f else 0.45f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (currentStep >= 3) Primary.copy(alpha = 0.12f)
                                else BorderColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (currentStep >= 3) Primary else TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Selfie de Verificación",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStep >= 3) TextMain else TextMuted
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Tómate una foto sosteniendo tu DNI\njunto a tu rostro",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    if (currentStep == 3) {
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (state.selfieUri != null) SuccessColor else Primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(Primary.copy(alpha = 0.04f))
                                .clickable {
                                    val uri = createUri()
                                    tempUri = uri
                                    launchCameraWithPermission(selfieLauncher, uri)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.selfieUri != null) {
                                AsyncImage(
                                    model = state.selfieUri,
                                    contentDescription = "Selfie",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Toca para abrir la cámara",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.submitKyc(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = state.selfieUri != null && !state.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Enviar Verificación",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WarningColor.copy(alpha = 0.08f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "!", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WarningColor)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Tus documentos son procesados de forma segura y encriptada. Solo se utilizan para verificar tu identidad.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Completar después",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun KycStepDot(
    stepIndex: Int,
    currentStep: Int,
    label: String
) {
    val isCompleted = currentStep > stepIndex
    val isActive = currentStep == stepIndex

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> SuccessColor
                        isActive -> Primary
                        else -> BorderColor
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$stepIndex",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else TextMuted
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = when {
                isCompleted -> SuccessColor
                isActive -> Primary
                else -> TextMuted
            },
            fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun KycStepLine(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 0.dp)
            .padding(bottom = 18.dp)
            .width(40.dp)
            .height(2.dp)
            .background(if (isCompleted) SuccessColor else BorderColor)
    )
}

@Composable
private fun KycStepCard(
    isActive: Boolean,
    isCompleted: Boolean,
    stepNumber: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> SuccessColor.copy(alpha = 0.04f)
                isActive -> SurfaceColor
                else -> SurfaceColor
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 6.dp else 2.dp
        ),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.4f))
        } else if (isCompleted) {
            androidx.compose.foundation.BorderStroke(1.dp, SuccessColor.copy(alpha = 0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
