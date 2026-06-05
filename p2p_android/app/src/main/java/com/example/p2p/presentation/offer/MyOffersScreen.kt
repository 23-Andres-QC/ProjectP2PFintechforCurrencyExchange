package com.example.p2p.presentation.offer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(
    viewModel: MyOffersViewModel? = null,
    onBack: () -> Unit = {},
    onPublishClick: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = MyOffersUiState())
        ?: remember { mutableStateOf(MyOffersUiState()) }

    LaunchedEffect(Unit) { viewModel?.loadMyOffers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ofertas", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onPublishClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Publicar Nueva Oferta", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Filtros ──────────────────────────────────────────────
            item {
                FilterBar(
                    activeFilter = uiState.activeFilter,
                    totalCount = uiState.offers.size,
                    activeCount = uiState.offers.count { it.status == "active" },
                    pausedCount = uiState.offers.count { it.status == "paused" },
                    onFilterChange = { viewModel?.setFilter(it) }
                )
            }

            // ── Contenido ─────────────────────────────────────────────
            when {
                uiState.isLoading && uiState.offers.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.error != null -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}", color = DangerColor)
                    }
                }
                uiState.filteredOffers.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (uiState.activeFilter) {
                                OfferFilter.ACTIVE -> "No tienes ofertas activas"
                                OfferFilter.PAUSED -> "No tienes ofertas pausadas"
                                OfferFilter.ALL    -> "No tienes ofertas publicadas"
                            },
                            color = TextMuted
                        )
                    }
                }
                else -> items(uiState.filteredOffers) { offer ->
                    OfferCard(
                        offer = offer,
                        onPauseResume = {
                            if (offer.status == "active") viewModel?.pauseOffer(offer.id)
                            else viewModel?.resumeOffer(offer.id)
                        },
                        onDelete = { viewModel?.deleteOffer(offer.id) }
                    )
                }
            }
        }
    }
}

// ── FilterBar ────────────────────────────────────────────────────────────────

@Composable
private fun FilterBar(
    activeFilter: OfferFilter,
    totalCount: Int,
    activeCount: Int,
    pausedCount: Int,
    onFilterChange: (OfferFilter) -> Unit
) {
    val filters = listOf(
        Triple(OfferFilter.ALL,    "Todas",   totalCount),
        Triple(OfferFilter.ACTIVE, "Activas", activeCount),
        Triple(OfferFilter.PAUSED, "Pausadas", pausedCount)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filters.forEach { (filter, label, count) ->
            val isSelected = activeFilter == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Primary else Color.Transparent)
                    .then(
                        if (!isSelected) Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Hacemos clickable con OutlinedButton invisible debajo
                TextButton(
                    onClick = { onFilterChange(filter) },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted
                        )
                        Text(
                            text = "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ── OfferCard ────────────────────────────────────────────────────────────────

@Composable
private fun OfferCard(
    offer: Offer,
    onPauseResume: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isActive = offer.status == "active"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Divisa + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${offer.currency} → ${offer.fiat_currency}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val statusColor = if (isActive) SuccessColor else WarningColor
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isActive) "Activa" else "Pausada",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tasa + disponible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${offer.fiat_currency} ${String.format("%.3f", offer.price_per_unit)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextMain
                )
                Text(
                    text = "${String.format("%.2f", offer.available_amount)} ${offer.currency} disp.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Min ${offer.fiat_currency} ${String.format("%.2f", offer.min_transaction)} · Max ${offer.fiat_currency} ${String.format("%.2f", offer.max_transaction ?: offer.amount)}",
                fontSize = 12.sp,
                color = TextMuted
            )

            // Tipo de oferta
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (offer.offer_type == "full") "Venta completa" else "Venta por partes",
                fontSize = 11.sp,
                color = Primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(14.dp))

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPauseResume,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WarningColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isActive) "Pausar" else "Reanudar",
                        fontSize = 12.sp,
                        color = WarningColor
                    )
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DangerColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Eliminar", fontSize = 12.sp, color = DangerColor)
                }
            }
        }
    }
}