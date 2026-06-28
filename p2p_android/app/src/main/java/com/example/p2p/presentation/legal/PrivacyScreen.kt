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
                body = "Los datos recopilados son utilizados exclusivamente para: verificar su identidad y cumplir con las obligaciones legales AML/KYC; procesar y gestionar sus transacciones de cambio de divisas; enviar notificaciones transaccionales y alertas de seguridad; mejorar la experiencia de usuario en la plataforma; cumplir con requerimientos de las autoridades regulatorias (SBS, BCRP, UIF-Perú); detectar y prevenir actividades fraudulentas."
            )
            PrivacySection(
                title = "3. Almacenamiento y Seguridad",
                body = "Su información es almacenada en servidores seguros con cifrado AES-256 y protección TLS 1.3 en tránsito. Implementamos controles de acceso estrictos, autenticación de doble factor para administradores y auditorías de seguridad periódicas. Los datos de transacciones son conservados por un mínimo de 5 años conforme a la normativa de prevención de lavado de activos. Los documentos KYC se almacenan con cifrado adicional en repositorios aislados."
            )
            PrivacySection(
                title = "4. Derechos del Usuario",
                body = "De acuerdo con la Ley N° 29733 de Protección de Datos Personales del Perú, usted tiene derecho a: acceder a sus datos personales almacenados; solicitar la rectificación de datos inexactos; solicitar la cancelación o eliminación de sus datos cuando corresponda; oponerse al tratamiento de sus datos en determinadas circunstancias; solicitar la portabilidad de su información. Para ejercer estos derechos, contáctenos a privacidad@peruexchange.com."
            )
            PrivacySection(
                title = "5. Compartición con Terceros",
                body = "No vendemos, alquilamos ni cedemos sus datos personales a terceros con fines comerciales. Solo compartimos información con: entidades financieras participantes en sus transacciones; autoridades regulatorias cuando sea legalmente requerido (SBS, UIF-Perú, Poder Judicial); proveedores de servicios tecnológicos bajo estrictos acuerdos de confidencialidad; servicios de verificación de identidad acreditados."
            )
            PrivacySection(
                title = "6. Cookies y Tecnologías de Seguimiento",
                body = "Utilizamos cookies esenciales para el funcionamiento de la plataforma y cookies analíticas para mejorar nuestros servicios. Puede configurar su dispositivo para rechazar cookies no esenciales, aunque esto podría afectar algunas funcionalidades. No utilizamos tecnologías de seguimiento con fines publicitarios de terceros."
            )
            PrivacySection(
                title = "7. Modificaciones a esta Política",
                body = "PeruExchange S.A.C. se reserva el derecho de actualizar esta Política de Privacidad cuando sea necesario. Le notificaremos sobre cambios significativos mediante correo electrónico o aviso prominente en la plataforma con al menos 15 días de anticipación. El uso continuado de la plataforma tras la entrada en vigencia de los cambios constituye su aceptación de la nueva política."
            )
            PrivacySection(
                title = "8. Contacto",
                body = "Para consultas, solicitudes o reclamos relacionados con el tratamiento de sus datos personales, puede comunicarse con nuestro Responsable de Protección de Datos: privacidad@peruexchange.com. También puede contactarnos en nuestras oficinas: Av. Javier Prado Este 1234, San Isidro, Lima, Perú. Plazo de respuesta: máximo 5 días hábiles."
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
