package com.example.p2p.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Política de Privacidad",
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
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Política de Privacidad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    "Última actualización: Mayo 2026",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            PrivacySection(
                title = "1. Recopilación de Datos",
                body = "PeruExchange P2P recopila información personal necesaria para la prestación de sus servicios, incluyendo: nombre completo, número de documento de identidad (DNI/CE), dirección de correo electrónico, número de teléfono, datos bancarios para las transacciones, dirección IP y datos de navegación en la plataforma. También recopilamos documentos requeridos para el proceso KYC conforme a la normativa vigente."
            )
            PrivacySection(
                title = "2. Uso de la Información",
                body = "Los datos recopilados se utilizan para verificar identidad, gestionar cuentas, ofertas, transacciones, comprobantes, calificaciones, reclamos, disputas y notificaciones dentro de la plataforma. También pueden usarse para seguridad, auditoría interna y prevención de uso indebido."
            )
            PrivacySection(
                title = "3. Almacenamiento y Seguridad",
                body = "La información se almacena en servicios protegidos y con controles de acceso de acuerdo con la configuración técnica de la plataforma. Los documentos, comprobantes y datos de operación se usan solo para los flujos necesarios de verificación, transacción, soporte, auditoría interna y administración."
            )
            PrivacySection(
                title = "4. Derechos del Usuario",
                body = "De acuerdo con la Ley N° 29733 de Protección de Datos Personales del Perú, usted tiene derecho a: acceder a sus datos personales almacenados; solicitar la rectificación de datos inexactos; solicitar la cancelación o eliminación de sus datos cuando corresponda; oponerse al tratamiento de sus datos en determinadas circunstancias; solicitar la portabilidad de su información. Para ejercer estos derechos, contáctenos a privacidad@peruexchange.com."
            )
            PrivacySection(
                title = "5. Compartición con Terceros",
                body = "No vendemos ni alquilamos sus datos personales con fines comerciales. Podemos compartir información cuando sea necesario para operar servicios tecnológicos de la plataforma, cumplir obligaciones legales o atender requerimientos válidos de una autoridad competente."
            )
            PrivacySection(
                title = "6. Cookies y Tecnologías de Seguimiento",
                body = "Utilizamos cookies esenciales para el funcionamiento de la plataforma y cookies analíticas para mejorar nuestros servicios. Puede configurar su dispositivo para rechazar cookies no esenciales, aunque esto podría afectar algunas funcionalidades. No utilizamos tecnologías de seguimiento con fines publicitarios de terceros."
            )
            PrivacySection(
                title = "7. Modificaciones a esta Política",
                body = "PeruExchange se reserva el derecho de actualizar esta Política de Privacidad cuando sea necesario. Los cambios relevantes podrán informarse mediante aviso dentro de la plataforma o por los canales de contacto disponibles."
            )
            PrivacySection(
                title = "8. Contacto",
                body = "Para consultas, solicitudes o reclamos relacionados con el tratamiento de sus datos personales, puede comunicarse por los canales de soporte de la app o escribir a privacidad@peruexchange.com."
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    GlassCard(
        shape = RoundedCornerShape(12.dp),
        elevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            Text(
                body,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 19.sp
            )
        }
    }
}
