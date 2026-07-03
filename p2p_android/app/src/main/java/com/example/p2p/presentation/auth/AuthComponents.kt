package com.example.p2p.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.p2p.R
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.TextSecond
import com.example.p2p.ui.theme.TextSubtle

@Composable
fun AuthBrandHeader(
    modifier: Modifier = Modifier,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 36.dp,
    logoContainerSize: Dp = 132.dp,
    logoSize: Dp = 124.dp,
    titleFontSize: TextUnit = 22.sp,
    subtitleFontSize: TextUnit = 12.sp,
    logoTextGap: Dp = 10.dp
) {
    val loginPrimary = Color(0xFF0B1330)
    val loginPrimaryDark = Color(0xFF06142F)
    val loginPrimaryLight = Color(0xFF0A3D91)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(loginPrimary, loginPrimaryDark, loginPrimaryLight)
                )
            )
            .statusBarsPadding()
            .padding(top = topPadding, bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(logoContainerSize)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_logo),
                contentDescription = "PeruExchange",
                modifier = Modifier.size(logoSize)
            )
        }

        Spacer(Modifier.height(logoTextGap))

        Text(
            text = "PeruExchange",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = titleFontSize,
            letterSpacing = 0.sp
        )
        Text(
            text = "Intercambio P2P · Lima, Perú",
            color = Color.White.copy(alpha = 0.82f),
            fontSize = subtitleFontSize,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AuthTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = TextMain,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
    }
}

@Composable
fun AuthFieldLabel(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecond, letterSpacing = 0.8.sp)
}

@Composable
fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = SurfaceColor,
    unfocusedContainerColor = SurfaceColor,
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    focusedPlaceholderColor = TextSubtle,
    unfocusedPlaceholderColor = TextSubtle,
    focusedLeadingIconColor = Primary,
    unfocusedLeadingIconColor = TextSecond,
    focusedTrailingIconColor = Primary,
    unfocusedTrailingIconColor = TextSecond,
    cursorColor = Primary,
    errorBorderColor = DangerColor,
    errorContainerColor = SurfaceColor,
    errorTextColor = TextMain,
    errorPlaceholderColor = TextSubtle,
)
