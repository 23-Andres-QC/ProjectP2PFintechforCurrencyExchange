package com.example.p2p.presentation.rating

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*

@Composable
fun RatingScreen(
    transactionId: String? = null,
    defaultScore: Int = 0,
    viewModel: RatingViewModel? = null,
    onSuccess: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(defaultScore.coerceIn(0, 5)) }
    var commentText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val uiState by viewModel?.uiState?.collectAsState(initial = RatingUiState()) ?: remember { mutableStateOf(RatingUiState()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(WarningColor.copy(alpha = 0.12f))
                .border(2.dp, WarningColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = WarningColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "¿Cómo fue la operación?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Califica al Vendedor",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = 3.dp,
        ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    val isSelected = starIndex <= score
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Calificar $starIndex",
                        tint = if (isSelected) WarningColor else BorderColor,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { score = starIndex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (score == 0) "Selecciona una calificación *" else when (score) {
                    1 -> "Muy malo"
                    2 -> "Malo"
                    3 -> "Regular"
                    4 -> "Bueno"
                    else -> "Excelente"
                },
                fontSize = 12.sp,
                color = if (score == 0) DangerColor else WarningColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comentario (opcional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
                Text(
                    text = "${commentText.length}/200",
                    fontSize = 11.sp,
                    color = if (commentText.length >= 200) DangerColor else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = commentText,
                onValueChange = { if (it.length <= 200) commentText = it },
                placeholder = {
                    Text(
                        text = "¿Cómo fue la experiencia con este vendedor?",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain,
                    cursorColor = Primary
                )
            )
        }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirmar calificación", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Vas a enviar ${score} estrella${if (score != 1) "s" else ""} al vendedor. Esta acción no se puede deshacer.",
                        fontSize = 14.sp,
                        color = TextMain
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            if (transactionId != null) {
                                viewModel?.submitRating(
                                    transactionId = transactionId,
                                    score = score,
                                    comment = commentText.ifBlank { null },
                                    onSuccess = {
                                        Toast.makeText(context, "Calificación enviada", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                onSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                }
            )
        }

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !uiState.isLoading && score > 0
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enviar Calificación",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

