package com.example.p2p.presentation.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.TextSecond
import com.example.p2p.ui.theme.TextSubtle

@Composable
fun AuthTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
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
