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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.navigation.Screen
import com.example.p2p.presentation.common.RefreshOnResume
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

    RefreshOnResume {
        viewModel?.loadProfile()
        viewModel?.loadUnreadCount()
    }

    val fullName = user?.full_name ?: "Usuario"
    val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    val email = user?.email ?: "cargando..."
    val ratingStr = user?.rating?.toString() ?: "5.0"
    val txCount = user?.total_transactions?.toString() ?: "0"
    val roleStr = if (user?.role == "vendor") "Experto" else "Básico"
    val isVerified = user?.kyc_verified == true
    val isAdmin = user?.role == "admin"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 68.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(top = 80.dp, bottom = 18.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(fullName, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                    Text(email, color = TextMuted, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileBadge(ratingStr, WarningColor, icon = Icons.Default.Star)
                    ProfileBadge(roleStr, Primary)
                    if (isVerified) {
                        ProfileBadge("Verificado", SuccessColor, icon = Icons.Default.CheckCircle)
                    }
                }
                HorizontalDivider(color = BorderColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(txCount, "Operaciones", Icons.Default.SwapHoriz)
                    VerticalDividerLine()
                    StatColumn(ratingStr, "Calificación")
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 50.dp)
                    .size(84.dp)
                    .shadow(elevation = 14.dp, shape = CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryDark)))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            }
        }

        com.example.p2p.ui.components.GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigate(if (isVerified) Screen.KycSummary.route else Screen.Kyc.route) },
            tint = if (isVerified) SuccessColor else Primary,
            contentPadding = PaddingValues(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.example.p2p.ui.components.GlassIconBadge(
                    icon = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Shield,
                    tint = if (isVerified) SuccessColor else Primary,
                    size = 36.dp, iconSize = 18.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isVerified) "KYC Verificado" else "Verifica tu identidad",
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = if (isVerified) SuccessColor else Primary
                    )
                    Text(
                        if (isVerified) "Límites ampliados · Cuenta Premium" else "Completa tu KYC para operar sin límites",
                        fontSize = 11.sp, color = TextMuted
                    )
                }
                Icon(
                    Icons.Default.ChevronRight, contentDescription = null,
                    tint = if (isVerified) SuccessColor else Primary, modifier = Modifier.size(16.dp)
                )
            }
        }

        if (isAdmin) {
            // El acceso al Panel Administrador ya vive en la barra inferior (pestana "Admin"),
            // no hace falta repetirlo aqui.
        } else {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate(Screen.MyDisputes.route) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerColor),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Mis Disputas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            MenuSection(title = "MI CUENTA") {
                MenuItem(
                    icon = Icons.Default.CreditCard, iconTint = Primary,
                    label = "Tarjetas y Cuentas",
                    onClick = { onNavigate(Screen.BankAccounts.route) }
                )
                MenuItem(
                    icon = Icons.Default.History, iconTint = PrimaryLight,
                    label = "Historial de Operaciones",
                    onClick = { onNavigate(Screen.History.createRoute()) }
                )
                MenuItem(
                    icon = Icons.Default.Star, iconTint = WarningColor,
                    label = "Mis Reseñas",
                    onClick = { onNavigate(Screen.Reviews.route) }
                )
                MenuItem(
                    icon = Icons.Default.Campaign, iconTint = SuccessColor,
                    label = "Mis Ofertas",
                    onClick = { onNavigate(Screen.MyOffers.route) },
                    showDivider = false
                )
            }

            MenuSection(title = "SOPORTE") {
                MenuItem(
                    icon = Icons.Default.HeadsetMic, iconTint = Primary,
                    label = "Reclamos",
                    onClick = { onNavigate(Screen.Complaints.route) }
                )
                MenuItem(
                    icon = Icons.Default.Notifications, iconTint = WarningColor,
                    label = "Notificaciones",
                    badge = unreadCount,
                    onClick = { onNavigate(Screen.Notifications.route) },
                    showDivider = false
                )
            }

            MenuSection(title = "LEGAL") {
                MenuItem(
                    icon = Icons.Default.Description, iconTint = Primary,
                    label = "Términos y Condiciones",
                    onClick = { onNavigate(Screen.Terms.route) }
                )
                MenuItem(
                    icon = Icons.Default.Lock, iconTint = Primary,
                    label = "Política de Privacidad",
                    onClick = { onNavigate(Screen.Privacy.route) }
                )
                MenuItem(
                    icon = Icons.Default.Info, iconTint = PrimaryLight,
                    label = "Acerca de Perú Exchange",
                    onClick = { onNavigate(Screen.About.route) }
                )
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline, iconTint = SuccessColor,
                    label = "Centro de Ayuda",
                    onClick = { onNavigate(Screen.Help.route) },
                    showDivider = false
                )
            }
        }

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

@Composable
private fun ProfileBadge(text: String, tint: Color, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        }
        Text(text, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RowScope.StatColumn(value: String, label: String, icon: ImageVector = Icons.Default.Star) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(value, color = Primary, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(modifier = Modifier.width(1.dp).height(34.dp).background(BorderColor))
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
        com.example.p2p.ui.components.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            elevation = 6.dp,
        ) { content() }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
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
            com.example.p2p.ui.components.GlassIconBadge(icon = icon, tint = iconTint, size = 40.dp, iconSize = 20.dp, contentDescription = label)
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
