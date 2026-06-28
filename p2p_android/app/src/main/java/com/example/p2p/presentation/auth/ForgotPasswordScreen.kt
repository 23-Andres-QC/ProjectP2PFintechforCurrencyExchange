package com.example.p2p.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    fun sendReset() {
        if (email.isBlank()) return
        error = null
        isLoading = true
        scope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(email.trim()).await()
                sent = true
            } catch (e: Exception) {
                error = when {
                    e.message?.contains("no user record") == true ||
                    e.message?.contains("user-not-found") == true ->
                        "No existe una cuenta con ese correo"
                    e.message?.contains("invalid-email") == true ->
                        "El correo electrónico no es válido"
                    e.message?.contains("network") == true ->
                        "Sin conexión a internet"
                    else -> "Error al enviar el correo. Intenta de nuevo."
                }
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthTopBar(
                title = "Recuperar Contraseña",
                onBack = onNavigateBack,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(28.dp))
                    .background(if (sent) SuccessColor.copy(alpha = 0.10f) else Primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (sent) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (sent) SuccessColor else Primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (sent)
                    "¡Correo enviado! Revisa tu bandeja de entrada y sigue las instrucciones."
                else
                    "Ingresa tu correo para recibir un enlace de restablecimiento seguro.",
                fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center,
                lineHeight = 22.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                elevation = 4.dp,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    if (sent) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            tint = SuccessColor,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, null, tint = SuccessColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Enlace enviado a $email", fontSize = 12.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Volver al Login", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    } else {
                        AuthFieldLabel("CORREO ELECTRÓNICO")

                        OutlinedTextField(
                            value = email, onValueChange = { email = it; error = null },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = TextMuted) },
                            placeholder = { Text("tu@email.com", color = TextMuted, fontSize = 13.sp) },
                            isError = error != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        error?.let {
                            Text(it, fontSize = 12.sp, color = DangerColor, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { sendReset() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            enabled = email.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Enviar Enlace", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!sent) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Primary.copy(alpha = 0.06f)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Recibirás instrucciones en menos de 5 minutos.", fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
