package com.example.p2p.presentation.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.api.ReceivedRating
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    viewModel: ReviewsViewModel? = null,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = ReviewsUiState())
        ?: remember { mutableStateOf(ReviewsUiState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reseñas", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = TextMain)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel?.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        val errorMsg = uiState.error
        if (errorMsg != null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("No se pudieron cargar las reseñas", fontWeight = FontWeight.SemiBold, color = TextMain)
                    Text(errorMsg, fontSize = 12.sp, color = TextMuted)
                    Button(onClick = { viewModel?.load() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                        Text("Reintentar")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.total > 0) String.format("%.1f", uiState.average) else "--",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "(${uiState.total} reseñas)",
                            fontSize = 14.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        val fullStars = uiState.average.toInt()
                        repeat(5) { i ->
                            Icon(
                                if (i < fullStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = WarningColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    listOf(5, 4, 3, 2, 1).forEach { stars ->
                        val count = uiState.distribution[stars.toString()] ?: 0
                        RatingBar(stars = stars, count = count, total = uiState.total)
                    }
                }
            }

            Text("Comentarios Recibidos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)

            if (uiState.total == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(WarningColor.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.StarBorder, contentDescription = null, tint = WarningColor, modifier = Modifier.size(26.dp))
                    }
                    Text(
                        "Aún no tienes reseñas. Completa transacciones para recibirlas.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                uiState.ratings.forEach { rating ->
                    ReviewItem(rating = rating)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RatingBar(stars: Int, count: Int, total: Int) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.width(34.dp)
        ) {
            Text("$stars", fontSize = 12.sp, color = TextMuted)
            Icon(Icons.Default.Star, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BorderColor)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(WarningColor)
                )
            }
        }
        Text("$count", fontSize = 12.sp, color = TextMuted, modifier = Modifier.width(20.dp))
    }
}

@Composable
private fun ReviewItem(rating: ReceivedRating) {
    val name = rating.rater_name ?: "Anónimo"
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "??" }
    val date = rating.created_at?.take(10) ?: ""

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    Text(date, fontSize = 11.sp, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(5) { i ->
                        Icon(
                            if (i < rating.score) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = WarningColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (!rating.comment.isNullOrBlank()) {
                    Text(rating.comment, fontSize = 13.sp, color = TextMuted, lineHeight = 18.sp)
                }
            }
        }
    }
}
