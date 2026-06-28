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
fun TermsScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Términos y Condiciones",
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
                    "Términos de Servicio",
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

            LegalSection(
                title = "1. Aceptación de Términos",
                body = "Al acceder y utilizar la plataforma PeruExchange P2P, usted acepta estar vinculado por estos Términos de Servicio y todas las leyes y regulaciones aplicables. Si no está de acuerdo con alguno de estos términos, tiene prohibido usar o acceder a este sitio. Los materiales contenidos en esta plataforma están protegidos por las leyes de propiedad intelectual aplicables."
            )
            LegalSection(
                title = "2. Descripción del Servicio",
                body = "PeruExchange P2P es una plataforma de intermediación para el intercambio de divisas entre personas naturales y jurídicas. Facilitamos operaciones de compra y venta de moneda extranjera de manera directa entre usuarios, cumpliendo con todas las normativas del Banco Central de Reserva del Perú (BCRP) y la Superintendencia de Banca, Seguros y AFP (SBS)."
            )
            LegalSection(
                title = "3. Elegibilidad",
                body = "Para utilizar nuestros servicios debe: (a) tener al menos 18 años de edad; (b) ser residente en el territorio peruano o contar con documentación válida para operar en Perú; (c) completar exitosamente el proceso de verificación de identidad KYC; (d) no estar incluido en listas de restricción financiera nacionales o internacionales."
            )
            LegalSection(
                title = "4. Responsabilidades del Usuario",
                body = "El usuario se compromete a: proporcionar información veraz y actualizada durante el registro; mantener la confidencialidad de sus credenciales de acceso; notificar de manera inmediata cualquier uso no autorizado de su cuenta; cumplir con los plazos acordados en cada transacción; no utilizar la plataforma para fines ilícitos o contrarios a la legislación vigente."
            )
            LegalSection(
                title = "5. Limitación de Responsabilidad",
                body = "PeruExchange P2P actúa exclusivamente como intermediario entre compradores y vendedores. La plataforma no es responsable por demoras, incumplimientos o pérdidas derivadas de acciones de terceros, fallas en servicios bancarios externos, eventos de fuerza mayor o problemas técnicos fuera de nuestro control. El límite máximo de responsabilidad de la plataforma no excederá el monto de las comisiones cobradas."
            )
            LegalSection(
                title = "6. Prohibiciones",
                body = "Está estrictamente prohibido: usar la plataforma para lavado de activos o financiamiento del terrorismo; realizar transacciones con fondos de origen ilícito; crear múltiples cuentas para evadir límites operativos; manipular precios o tasas de cambio; publicar ofertas fraudulentas; utilizar bots o herramientas automatizadas no autorizadas; acceder a cuentas de terceros sin autorización."
            )
            LegalSection(
                title = "7. Resolución de Disputas",
                body = "En caso de disputa entre usuarios, PeruExchange P2P actuará como mediador imparcial. El proceso de resolución tiene un plazo máximo de 5 días hábiles desde la apertura del caso. Durante este período, los fondos en custodia permanecerán bloqueados. La decisión del equipo de resolución es vinculante para ambas partes. En casos de fraude comprobado, la cuenta infractora será suspendida de forma definitiva."
            )
            LegalSection(
                title = "8. Contacto",
                body = "Para consultas relacionadas con estos Términos de Servicio, puede contactarnos a través de nuestro correo oficial: legal@peruexchange.com. Nuestro equipo legal responderá en un plazo máximo de 3 días hábiles. También puede escribirnos al Departamento Legal de PeruExchange S.A.C., Av. Javier Prado Este 1234, San Isidro, Lima, Perú."
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LegalSection(title: String, body: String) {
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
