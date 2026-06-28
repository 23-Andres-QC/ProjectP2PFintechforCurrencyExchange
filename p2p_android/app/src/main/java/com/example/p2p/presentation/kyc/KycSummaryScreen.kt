package com.example.p2p.presentation.kyc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.p2p.presentation.common.CONTRACT_TEXT
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.components.GlassIconBadge
import com.example.p2p.ui.theme.*

@Composable
fun KycSummaryScreen(
    onNavigateBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    viewModel: KycSummaryViewModel? = null,
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = KycSummaryUiState())
        ?: remember { mutableStateOf(KycSummaryUiState()) }
    val user = uiState.user
    var contractExpanded by remember { mutableStateOf(false) }

    Scaffold(containerColor = BackgroundApp) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Primary.copy(alpha = 0.10f), BackgroundApp)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
                }
                Spacer(Modifier.width(4.dp))
                Text("Mi verificación KYC", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), tint = SuccessColor) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassIconBadge(icon = Icons.Default.CheckCircle, tint = SuccessColor)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Identidad verificada", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                        Text("Límites ampliados · Cuenta Premium", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("MIS DATOS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(10.dp))
                KycDataRow(label = "Nombre completo", value = user?.full_name ?: "—")
                KycDataRow(label = "DNI", value = user?.dni ?: "No registrado")
                KycDataRow(label = "Correo", value = user?.email ?: "—")

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    KycDocThumb("DNI Frontal", user?.dni_image_url, Modifier.weight(1f))
                    KycDocThumb("DNI Dorso", user?.dni_back_url, Modifier.weight(1f))
                    KycDocThumb("Selfie", user?.selfie_url, Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar mis datos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassIconBadge(icon = Icons.Default.Description, tint = Primary, size = 36.dp, iconSize = 18.dp)
                        Text("Contrato firmado", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                    }
                    IconButton(onClick = { contractExpanded = !contractExpanded }) {
                        Icon(
                            if (contractExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null, tint = Primary
                        )
                    }
                }

                if (contractExpanded) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(CONTRACT_TEXT, fontSize = 11.sp, color = TextMuted, lineHeight = 16.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("FIRMA ELECTRÓNICA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.signature_url != null) {
                        AsyncImage(
                            model = user.signature_url,
                            contentDescription = "Firma del usuario",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Draw, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Aún no tienes una firma registrada", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun KycDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
    }
}

@Composable
private fun KycDocThumb(label: String, url: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (url != null) {
                AsyncImage(model = url, contentDescription = label, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}
