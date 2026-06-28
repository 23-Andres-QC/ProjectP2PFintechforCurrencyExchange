package com.example.p2p.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.components.GlassIconBadge
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Acerca de Perú Exchange",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextMain
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "PE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Perú Exchange P2P",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    "Fintech de Cambio de Divisas · v2.1.4",
                    fontSize = 12.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                "PeruExchange P2P es la plataforma líder en intercambio de divisas entre personas en el Perú. " +
                    "Conectamos compradores y vendedores de manera directa, ofreciendo tasas competitivas, " +
                    "seguridad garantizada y transacciones verificadas con inteligencia artificial. " +
                    "Operamos bajo la supervisión de la SBS y el BCRP.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            Text(
                "Nuestras Características",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                modifier = Modifier.align(Alignment.Start)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        icon = Icons.Default.Security,
                        iconColor = SuccessColor,
                        title = "Regulados SBS",
                        description = "Supervisados por la Superintendencia de Banca",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.Bolt,
                        iconColor = WarningColor,
                        title = "Tecnología IA",
                        description = "Verificación OCR y detección de fraude con IA",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        icon = Icons.Default.Lock,
                        iconColor = Primary,
                        title = "P2P Seguro",
                        description = "Fondos en custodia hasta confirmar la transacción",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        icon = Icons.Default.HeadsetMic,
                        iconColor = PrimaryLight,
                        title = "Soporte 24/7",
                        description = "Atención continua todos los días del año",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "© 2026 Perú Exchange S.A.C.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Text(
                    "RUC: 20601234567 · Lima, Perú",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(14.dp),
        elevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconBadge(icon = icon, tint = iconColor, size = 36.dp, iconSize = 20.dp)
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            Text(
                description,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
