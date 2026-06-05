package com.example.p2p.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.navigation.Screen
import com.example.p2p.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel? = null,
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = ProfileUiState()) ?: remember { mutableStateOf(ProfileUiState()) }
    val unreadCount = uiState.unreadNotifications
    val user = uiState.user

    val fullName = user?.full_name ?: "Usuario"
    val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    val email = user?.email ?: "cargando..."
    val ratingStr = user?.rating?.toString() ?: "5.0"
    val txCount = user?.total_transactions?.toString() ?: "0"
    val roleStr = if (user?.role == "vendor") "Experto" else "Básico"
    val isVerified = user?.kyc_verified == true
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── User hero card ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
                Text(fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(email, color = PrimaryMint, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileBadge("⭐ $ratingStr", Color.White.copy(alpha = 0.2f), Color.White)
                    ProfileBadge(roleStr, PrimaryMint.copy(alpha = 0.2f), PrimaryMint)
                    if (isVerified) {
                        ProfileBadge("✓ Verificado", Color.White.copy(alpha = 0.15f), Color.White)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(txCount, "Operaciones")
                    VerticalDividerLine()
                    StatColumn("100%", "Completadas")
                    VerticalDividerLine()
                    StatColumn("~1m", "Respuesta")
                }
            }
        }

        // ── KYC Banner ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SuccessColor.copy(alpha = 0.1f))
                .border(1.dp, SuccessColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onNavigate(Screen.Kyc.route) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(SuccessColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("KYC Verificado", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SuccessColor)
                Text("Límites ampliados · Cuenta Premium", fontSize = 11.sp, color = TextMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
        }

        // ── Quick Actions ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavigate(Screen.History.route) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarningColor),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Pendientes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { onNavigate(Screen.MyDisputes.route) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Mis Disputas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Mi Cuenta ────────────────────────────────────────────────────────
        MenuSection(title = "MI CUENTA") {
            MenuItem(
                icon = Icons.Default.CreditCard, iconBg = Primary.copy(.12f), iconTint = Primary,
                label = "Tarjetas y Cuentas",
                onClick = { onNavigate(Screen.BankAccounts.route) }
            )
            MenuItem(
                icon = Icons.Default.History, iconBg = PrimaryLight.copy(.15f), iconTint = PrimaryLight,
                label = "Historial de Operaciones",
                onClick = { onNavigate(Screen.History.route) }
            )
            MenuItem(
                icon = Icons.Default.Star, iconBg = WarningColor.copy(.12f), iconTint = WarningColor,
                label = "Mis Reseñas",
                onClick = { onNavigate(Screen.Reviews.route) }
            )
            MenuItem(
                icon = Icons.Default.Campaign, iconBg = SuccessColor.copy(.12f), iconTint = SuccessColor,
                label = "Mis Ofertas",
                onClick = { onNavigate(Screen.MyOffers.route) },
                showDivider = false
            )
        }

        // ── Soporte ───────────────────────────────────────────────────────────
        MenuSection(title = "SOPORTE") {
            MenuItem(
                icon = Icons.Default.Store, iconBg = WarningColor.copy(.12f), iconTint = WarningColor,
                label = "Modo Vendedor (Inbox)",
                onClick = { onNavigate(Screen.Vendor.route) }
            )
            MenuItem(
                icon = Icons.Default.HeadsetMic, iconBg = Primary.copy(.12f), iconTint = Primary,
                label = "Reclamos",
                onClick = { onNavigate(Screen.Complaints.route) }
            )
            MenuItem(
                icon = Icons.Default.Notifications, iconBg = WarningColor.copy(.12f), iconTint = WarningColor,
                label = "Notificaciones",
                badge = unreadCount,
                onClick = { onNavigate(Screen.Notifications.route) }
            )
            MenuItem(
                icon = Icons.Default.AdminPanelSettings, iconBg = DangerColor.copy(.1f), iconTint = DangerColor,
                label = "Panel Administrador",
                onClick = { onNavigate(Screen.Admin.route) },
                showDivider = false
            )
        }

        // ── Legal ─────────────────────────────────────────────────────────────
        MenuSection(title = "LEGAL") {
            MenuItem(
                icon = Icons.Default.Description, iconBg = Primary.copy(.1f), iconTint = Primary,
                label = "Términos y Condiciones",
                onClick = { onNavigate(Screen.Terms.route) }
            )
            MenuItem(
                icon = Icons.Default.Lock, iconBg = Primary.copy(.1f), iconTint = Primary,
                label = "Política de Privacidad",
                onClick = { onNavigate(Screen.Privacy.route) }
            )
            MenuItem(
                icon = Icons.Default.Info, iconBg = PrimaryLight.copy(.12f), iconTint = PrimaryLight,
                label = "Acerca de Perú Exchange",
                onClick = { onNavigate(Screen.About.route) }
            )
            MenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline, iconBg = SuccessColor.copy(.1f), iconTint = SuccessColor,
                label = "Centro de Ayuda",
                onClick = { onNavigate(Screen.Help.route) },
                showDivider = false
            )
        }

        // ── Bottom Actions ────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigate(Screen.EditProfile.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Editar Perfil", fontWeight = FontWeight.SemiBold)
            }
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cerrar Sesión", color = DangerColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileBadge(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
}

@Composable
private fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp),
            letterSpacing = 1.sp
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    onClick: () -> Unit = {},
    showDivider: Boolean = true,
    badge: Int = 0,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(label, fontSize = 14.sp, color = TextMain, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(DangerColor)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badge > 99) "99+" else badge.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 66.dp, end = 14.dp), color = BorderColor, thickness = 0.5.dp)
        }
    }
}
