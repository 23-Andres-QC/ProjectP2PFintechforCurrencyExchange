package com.example.p2p.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.p2p.ui.theme.Primary

/**
 * Superficie "liquid glass" reutilizable: gradiente translúcido + borde con highlight
 * + sombra suave, usando siempre el mismo tinte (Primary por defecto) para mantener
 * un único lenguaje visual consistente en toda la app en vez de un color distinto por tarjeta.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tint: Color = Primary,
    shape: Shape = RoundedCornerShape(22.dp),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(shape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = tint.copy(alpha = 0.18f),
                shape = shape
            )
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Insignia circular con el mismo tratamiento "glass", para reemplazar los íconos de color sólido. */
@Composable
fun GlassIconBadge(
    icon: ImageVector,
    tint: Color = Primary,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null,
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 5.dp, shape = CircleShape, ambientColor = tint.copy(alpha = 0.55f), spotColor = tint.copy(alpha = 0.55f))
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(tint, tint.copy(alpha = 0.82f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}
