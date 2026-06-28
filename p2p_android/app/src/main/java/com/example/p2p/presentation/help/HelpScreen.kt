package com.example.p2p.presentation.help

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Centro de Ayuda",
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
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            GlassCard(
                shape = RoundedCornerShape(16.dp),
                elevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Canales de Atención Directa",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/51999999999?text=Hola,%20necesito%20ayuda%20con%20PeruExchange"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    "WhatsApp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text("24/7", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@peruexchange.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Soporte PeruExchange")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "support@\nperuexchange.com",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            Text(
                "Preguntas Frecuentes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            FaqItemExpanded(
                question = "¿Cómo funciona el sistema P2P?",
                answer = "El sistema P2P (Peer-to-Peer) conecta directamente a compradores y vendedores de divisas. El comprador publica una oferta o acepta una existente, transfiere los soles al vendedor y sube el comprobante. Nuestro sistema OCR verifica automáticamente el comprobante y libera los dólares al comprador. Todo el proceso es supervisado por nuestra plataforma para garantizar la seguridad de ambas partes."
            )

            FaqItemCollapsed("¿Cuáles son los límites de operación diarios?")
            FaqItemCollapsed("¿Cómo verifico mi identidad (KYC)?")
            FaqItemCollapsed("¿Qué hago si hay un problema con mi transacción?")

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FaqItemExpanded(question: String, answer: String) {
    GlassCard(
        shape = RoundedCornerShape(14.dp),
        elevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain,
                    modifier = Modifier.weight(1f)
                )
                com.example.p2p.ui.components.GlassIconBadge(
                    icon = Icons.Default.KeyboardArrowUp, tint = Primary, size = 28.dp, iconSize = 18.dp
                )
            }
            HorizontalDivider(color = BorderColor, thickness = 0.8.dp)
            Text(
                answer,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FaqItemCollapsed(question: String) {
    GlassCard(
        shape = RoundedCornerShape(14.dp),
        elevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                question,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
