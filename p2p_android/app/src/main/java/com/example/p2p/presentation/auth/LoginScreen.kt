package com.example.p2p.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fondo degradado oscuro (parte superior) ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0A1628), Color(0xFF0D2137), Color(0xFF0A3040))
                    )
                )
        )

        // ── Fondo claro (parte inferior) ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.BottomCenter)
                .background(BackgroundApp)
        )

        // ── Contenido principal ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(listOf(Primary, PrimaryMint))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PE",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "PeruExchange",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Intercambio P2P · Lima, Perú",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Tarjeta de login ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TextMain
                        )
                        Text(
                            text = "Crear cuenta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToRegister() }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                    Text(
                        text = "Bienvenido de nuevo",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                    )

                    // Error banner
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerColor.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                color = DangerColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    // Email
                    Text(
                        "CORREO ELECTRÓNICO",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null,
                                tint = Primary, modifier = Modifier.size(20.dp))
                        },
                        placeholder = { Text("tu@email.com", color = TextSubtle, fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Primary,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Contraseña
                    Text(
                        "CONTRASEÑA",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = TextMuted, letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null,
                                tint = Primary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = null, tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        placeholder = { Text("••••••••", color = TextSubtle, fontSize = 14.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus(); viewModel.login() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Primary,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    // Botón principal
                    Button(
                        onClick = { focusManager.clearFocus(); viewModel.login() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White, strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Ingresar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Credenciales demo ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.07f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CUENTAS DE PRUEBA",
                        fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = Primary, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        Pair("Comprador", "comprador@peruexchange.com  /  Comprador123!"),
                        Pair("Vendedor",  "vendedor@peruexchange.com   /  Vendedor123!"),
                        Pair("Admin",     "admin@peruexchange.com      /  Admin123!")
                    ).forEach { (rol, cred) ->
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                "$rol: ",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Text(cred, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = TextMuted, fontSize = 13.sp)) {
                        append("¿No tienes cuenta? ")
                    }
                    withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)) {
                        append("Regístrate")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToRegister() }
                    .padding(8.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
