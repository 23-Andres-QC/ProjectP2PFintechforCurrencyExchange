package com.example.p2p.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = RegisterUiState())
        ?: remember { mutableStateOf(RegisterUiState()) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top Bar ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextMain
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Registro de Usuario",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }

            // ── Main Card ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Full Name
                    FieldLabel("NOMBRE COMPLETO")
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted)
                        },
                        placeholder = { Text("Ej: Carlos Mendoza López", color = TextMuted, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = peruFieldColors(),
                        singleLine = true
                    )

                    // Email
                    FieldLabel("CORREO ELECTRÓNICO")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
                        },
                        placeholder = { Text("tu@email.com", color = TextMuted, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = peruFieldColors(),
                        singleLine = true
                    )

                    // DNI
                    FieldLabel("DNI")
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { if (it.length <= 8) dni = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted)
                        },
                        placeholder = { Text("12345678", color = TextMuted, fontSize = 13.sp) },
                        supportingText = {
                            Text(
                                "Requerido para verificación KYC",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = peruFieldColors(),
                        singleLine = true
                    )

                    // Password
                    FieldLabel("CONTRASEÑA")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        },
                        placeholder = { Text("Mínimo 8 caracteres", color = TextMuted, fontSize = 13.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = peruFieldColors(),
                        singleLine = true
                    )

                    // Confirm Password
                    FieldLabel("CONFIRMAR CONTRASEÑA")
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Mostrar confirmación",
                                    tint = TextMuted
                                )
                            }
                        },
                        placeholder = { Text("Repite tu contraseña", color = TextMuted, fontSize = 13.sp) },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                        supportingText = if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                            { Text("Las contraseñas no coinciden", fontSize = 11.sp, color = DangerColor) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = peruFieldColors(),
                        singleLine = true
                    )

                    // Terms Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = BorderColor
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            buildAnnotatedString {
                                append("Acepto los ")
                                withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) {
                                    append("Términos y Condiciones")
                                }
                            },
                            fontSize = 13.sp,
                            color = TextMain
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Register Button
                    Button(
                        onClick = {
                            if (password != confirmPassword) return@Button
                            viewModel?.register(email, password, fullName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryMint,
                            contentColor = Color(0xFF004D40)
                        ),
                        enabled = termsAccepted && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF004D40),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Continuar a Verificación", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    uiState.error?.let { err ->
                        Text(
                            text = err,
                            color = DangerColor,
                            fontSize = 12.sp,
                            modifier = androidx.compose.ui.Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Login link
            Text(
                buildAnnotatedString {
                    append("¿Ya tienes cuenta? ")
                    withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold)) {
                        append("Inicia sesión")
                    }
                },
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun peruFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = SurfaceColor,
    unfocusedContainerColor = SurfaceColor,
    errorBorderColor = DangerColor,
    errorContainerColor = SurfaceColor
)
