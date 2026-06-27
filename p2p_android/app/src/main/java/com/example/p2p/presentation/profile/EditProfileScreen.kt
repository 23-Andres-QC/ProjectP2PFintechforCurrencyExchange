package com.example.p2p.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel?.uiState?.collectAsState(initial = EditProfileUiState())
        ?: remember { mutableStateOf(EditProfileUiState()) }

    var fullNameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }

    RefreshOnResume {
        viewModel?.loadProfile()
    }

    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            if (fullNameText.isEmpty()) fullNameText = it.full_name ?: ""
            if (phoneText.isEmpty()) phoneText = it.phone ?: ""
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            viewModel?.resetSaveSuccess()
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }

    val user = uiState.user
    val initials = (user?.full_name ?: fullNameText)
        .split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifEmpty { "PE" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain) },
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                }
                TextButton(onClick = {}) {
                    Text("Cambiar foto", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    OutlinedTextField(
                        value = fullNameText,
                        onValueChange = { fullNameText = it },
                        label = { Text("Nombre Completo", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        )
                    )

                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        label = { Text("Correo Electrónico", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = BorderColor,
                            disabledLabelColor = TextMuted,
                            disabledTextColor = TextMuted,
                            disabledContainerColor = Color(0xFFF1F5F9)
                        )
                    )
                    Text(
                        "El correo no puede modificarse",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 4.dp, top = (-6).dp)
                    )

                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Teléfono", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        )
                    )

                    DropdownBox(label = "País", value = "Perú 🇵🇪")
                    DropdownBox(label = "Moneda preferida", value = "PEN")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = {},
                            colors = CheckboxDefaults.colors(checkedColor = Primary, uncheckedColor = BorderColor)
                        )
                        Text("Recibir notificaciones por correo", fontSize = 13.sp, color = TextMain, modifier = Modifier.weight(1f))
                    }
                }
            }

            Button(
                onClick = {
                    val name = fullNameText.trim()
                    if (name.isEmpty()) {
                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel?.saveProfile(name, phoneText.trim().ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar Cambios", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuccessColor.copy(alpha = 0.1f))
                    .border(1.dp, SuccessColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(18.dp))
                Text(
                    "✓ KYC Verificado · Puedes operar hasta \$10,000 USD/día",
                    fontSize = 12.sp,
                    color = SuccessColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DropdownBox(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(start = 2.dp, bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, fontSize = 14.sp, color = TextMain)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}
