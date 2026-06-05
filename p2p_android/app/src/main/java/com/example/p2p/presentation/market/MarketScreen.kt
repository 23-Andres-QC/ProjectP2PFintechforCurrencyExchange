package com.example.p2p.presentation.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.p2p.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2p.presentation.market.MarketViewModel
import com.example.p2p.data.remote.model.ExchangeRate
import com.example.p2p.data.remote.model.Offer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    userName: String = "Usuario",
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBuyDialog by remember { mutableStateOf<com.example.p2p.data.remote.model.Offer?>(null) }
    var buyAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadOffers()
    }

    val exchangeRates = uiState.exchangeRates

    Scaffold(
        containerColor = BackgroundApp,
        topBar = { MarketTopBar(exchangeRates = exchangeRates, onNavigateToNotifications = onNavigateToNotifications) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { WelcomeCard(userName = userName) }
            item { FilterCard() }
            item {
                MatchingRow(
                    onMatchingClick = {
                        viewModel.matchOffer(
                            currency = "USD",
                            fiatCurrency = "PEN",
                            onMatched = { matchedOffer ->
                                showBuyDialog = matchedOffer
                                buyAmount = matchedOffer.available_amount.toString()
                            },
                            onError = { err ->
                                Toast.makeText(context, "No hay ofertas: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
            
            if (uiState.isLoading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            } else if (uiState.error != null) {
                item { Text(text = uiState.error!!, color = DangerColor) }
            } else {
                item { OffersHeader(count = uiState.offers.size) }
                itemsIndexed(uiState.offers) { index, offer -> 
                    OfferCard(
                        offer = offer,
                        isBestRate = index == 0,
                        onConfirmBuy = { amount ->
                            val req = com.example.p2p.data.remote.model.CreateTransactionRequest(
                                offer_id = offer.id,
                                amount_from = amount,
                                amount_to = amount * offer.price_per_unit,
                                buyer_payment_account = "Mi Cuenta BCP",
                                vendor_payment_account = offer.payment_methods?.firstOrNull() ?: "BCP"
                            )
                            viewModel.createTransaction(req,
                                onSuccess = { txnId ->
                                    onNavigateToTransaction(txnId)
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) 
                }
            }
        }
    }

    if (showBuyDialog != null) {
        val offer = showBuyDialog!!
        val isPartial = offer.offer_type == "partial"
        var matchAmountText by remember(offer.id) { 
            mutableStateOf(if (isPartial) "" else offer.available_amount.toString()) 
        }
        val matchAmountDouble = matchAmountText.toDoubleOrNull() ?: 0.0
        val isMatchAmountValid = if (isPartial) {
            matchAmountDouble >= offer.min_transaction && 
            matchAmountDouble <= (offer.max_transaction ?: offer.available_amount) &&
            matchAmountDouble <= offer.available_amount
        } else {
            matchAmountDouble == offer.available_amount
        }

        AlertDialog(
            onDismissRequest = { showBuyDialog = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = WarningColor)
                    Text("Matching: Comprar USD") 
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vendedor: ${offer.vendor?.full_name ?: "Victor Vendedor"}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text("Tasa de cambio: S/ ${offer.price_per_unit}", fontSize = 14.sp)
                    Text("Disponible: ${offer.available_amount} USD", fontSize = 13.sp)
                    
                    if (isPartial) {
                        Text(
                            text = "Límites: ${offer.min_transaction} – ${offer.max_transaction ?: offer.available_amount} USD",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        OutlinedTextField(
                            value = matchAmountText,
                            onValueChange = { matchAmountText = it },
                            label = { Text("Monto a comprar (USD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain,
                                cursorColor = Primary
                            )
                        )
                    } else {
                        Text(
                            text = "Esta es una oferta de VENTA COMPLETA. Debes comprar el monto total disponible.",
                            fontSize = 12.sp,
                            color = WarningColor
                        )
                        OutlinedTextField(
                            value = matchAmountText,
                            onValueChange = {},
                            label = { Text("Monto a comprar (USD)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = BorderColor,
                                disabledTextColor = TextMain
                            )
                        )
                    }

                    if (matchAmountDouble > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessColor.copy(alpha = 0.08f))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recibirás aprox.", fontSize = 11.sp, color = TextMuted)
                                    Text(
                                        text = "S/ ${String.format("%.2f", matchAmountDouble * offer.price_per_unit)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessColor
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Primary)
                            }
                        }
                        
                        if (isPartial) {
                            if (matchAmountDouble < offer.min_transaction) {
                                Text("Monto mínimo es ${offer.min_transaction} USD", color = DangerColor, fontSize = 11.sp)
                            } else if (offer.max_transaction != null && matchAmountDouble > offer.max_transaction) {
                                Text("Monto máximo es ${offer.max_transaction} USD", color = DangerColor, fontSize = 11.sp)
                            } else if (matchAmountDouble > offer.available_amount) {
                                Text("Monto supera disponible (${offer.available_amount} USD)", color = DangerColor, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val req = com.example.p2p.data.remote.model.CreateTransactionRequest(
                            offer_id = offer.id,
                            amount_from = matchAmountDouble,
                            amount_to = matchAmountDouble * offer.price_per_unit,
                            buyer_payment_account = "Mi Cuenta BCP",
                            vendor_payment_account = offer.payment_methods?.firstOrNull() ?: "BCP"
                        )
                        viewModel.createTransaction(req,
                            onSuccess = { txnId ->
                                showBuyDialog = null
                                onNavigateToTransaction(txnId)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = isMatchAmountValid,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                ) {
                    Text("Confirmar compra")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
private fun MarketTopBar(
    exchangeRates: List<ExchangeRate> = emptyList(),
    onNavigateToNotifications: () -> Unit = {}
) {
    val tickerItems: List<Triple<String, String, Boolean>> = if (exchangeRates.isNotEmpty()) {
        val penRates = exchangeRates.filter { it.to_currency == "PEN" }
        if (penRates.isNotEmpty()) {
            penRates.map { r -> Triple(r.from_currency, "S/${String.format("%.3f", r.rate)}", true) }
        } else {
            listOf(Triple("USD", "S/3.720", true), Triple("EUR", "S/4.050", true))
        }
    } else {
        listOf(Triple("USD", "S/3.720", true), Triple("EUR", "S/4.050", true))
    }

    Surface(
        color = Color(0xFF0D1117),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Peru", color = TextMain, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Exchange", color = Primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNavigateToNotifications, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tickerItems.forEach { (currency, rate, up) ->
                    TickerItem(currency, rate, up)
                }
            }
        }
    }
}

@Composable
private fun TickerItem(currency: String, rate: String, up: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(currency, color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Text(rate, color = TextMain, fontSize = 11.sp)
        Text(
            if (up) "▲" else "▼",
            color = if (up) SuccessColor else DangerColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Welcome Card ────────────────────────────────────────────────────────────

@Composable
private fun WelcomeCard(userName: String = "Usuario") {
    val firstName = userName.split(" ").firstOrNull() ?: userName
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A2340), Color(0xFF0D1117))
                )
            )
            .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Primary.copy(alpha = 0.12f))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("⭐ Experto", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Bienvenido, $firstName", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("P2P Seguro · Lima, Perú", color = TextMuted, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.12f))
                    .border(2.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("PE", color = Primary, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
    }
}

// ─── Filter Card ─────────────────────────────────────────────────────────────

@Composable
private fun FilterCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Text("Filtrado Multidivisa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyDropdown("Tengo", "PEN", Modifier.weight(1f))
                CurrencyDropdown("Quiero", "USD", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                .background(BackgroundApp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Matching Row ─────────────────────────────────────────────────────────────

@Composable
private fun MatchingRow(onMatchingClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onMatchingClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningColor),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Matching Automático", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Sort, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Text("Mejor precio", fontSize = 12.sp, color = TextMuted)
        }
    }
}

// ─── Offers Header ────────────────────────────────────────────────────────────

@Composable
private fun OffersHeader(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ofertas disponibles", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Primary)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("$count", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Offer Card ───────────────────────────────────────────────────────────────

@Composable
private fun OfferCard(
    offer: com.example.p2p.data.remote.model.Offer,
    isBestRate: Boolean = false,
    onConfirmBuy: (Double) -> Unit
) {
    val isPartial = offer.offer_type == "partial"
    var isExpanded by remember { mutableStateOf(false) }
    var buyAmountText by remember { mutableStateOf("") }
    
    val buyAmountDouble = buyAmountText.toDoubleOrNull() ?: 0.0
    val isAmountValid = if (isPartial) {
        buyAmountDouble >= offer.min_transaction && 
        buyAmountDouble <= (offer.max_transaction ?: offer.available_amount) &&
        buyAmountDouble <= offer.available_amount
    } else {
        true
    }

    val initials = offer.vendor?.full_name?.trim()?.split(" ")
        ?.filter { it.isNotEmpty() }
        ?.take(2)
        ?.map { it.first().uppercaseChar() }
        ?.joinToString("") ?: "NN"

    val stars = offer.vendor?.rating ?: 4.9
    val trades = offer.vendor?.total_transactions ?: 180
    val verified = offer.vendor?.kyc_verified ?: true

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isBestRate) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarningColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🏆", fontSize = 11.sp)
                    Text("Mejor tasa del mercado", color = WarningColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            // Row 1: Avatar + name + rating
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = offer.vendor?.full_name ?: "Vendedor", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp, 
                            color = TextMain
                        )
                        if (verified) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle, 
                                contentDescription = "Verificado", 
                                tint = Primary, 
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text("★ $stars", fontSize = 11.sp, color = WarningColor, fontWeight = FontWeight.SemiBold)
                        Text("•  $trades ops", fontSize = 11.sp, color = TextMuted)
                        Text("•  ~2m", fontSize = 11.sp, color = TextMuted)
                        Text("•  98%", fontSize = 11.sp, color = SuccessColor, fontWeight = FontWeight.Medium)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(SuccessColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("En línea", color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
            
            // Row 2: Rate and Available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tasa", fontSize = 10.sp, color = TextMuted)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isPartial) Primary.copy(alpha = 0.1f) 
                                    else SuccessColor.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isPartial) "POR PARTES" else "COMPLETA", 
                                color = if (isPartial) Primary else SuccessColor, 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "S/ ${String.format("%.3f", offer.price_per_unit)}", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = TextMain
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Disponible: ${offer.available_amount} ${offer.currency}", fontSize = 11.sp, color = TextMuted)
                    if (isPartial) {
                        Text(
                            text = "Rango: ${offer.min_transaction} – ${offer.max_transaction ?: offer.available_amount} ${offer.currency}", 
                            fontSize = 9.sp, 
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Row 3: Bank methods + action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(offer.payment_methods?.firstOrNull() ?: "BCP", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                if (isPartial) {
                    Button(
                        onClick = { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isExpanded) "Cerrar" else "Elegir monto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = { onConfirmBuy(offer.available_amount) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Comprar todo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Row 4: Expandable Partial Purchase Panel
            if (isExpanded && isPartial) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .background(BackgroundApp)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Calculate, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "¿Cuánto deseas comprar?", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp, 
                            color = TextMain
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = buyAmountText,
                            onValueChange = { buyAmountText = it },
                            placeholder = { Text("Monto en ${offer.currency}", fontSize = 13.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain,
                                cursorColor = Primary
                            )
                        )
                        Text(
                            text = offer.currency, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = TextMuted
                        )
                    }

                    if (buyAmountDouble > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessColor.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recibirás aprox.", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "S/ ${String.format("%.2f", buyAmountDouble * offer.price_per_unit)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessColor
                                    )
                                }
                                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Primary)
                            }
                        }

                        // Warnings
                        if (buyAmountDouble < offer.min_transaction) {
                            Text("El mínimo es ${offer.min_transaction} ${offer.currency}", color = DangerColor, fontSize = 11.sp)
                        } else if (offer.max_transaction != null && buyAmountDouble > offer.max_transaction) {
                            Text("El máximo es ${offer.max_transaction} ${offer.currency}", color = DangerColor, fontSize = 11.sp)
                        } else if (buyAmountDouble > offer.available_amount) {
                            Text("Solo hay ${offer.available_amount} ${offer.currency} disponibles", color = DangerColor, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onConfirmBuy(buyAmountDouble)
                                isExpanded = false
                                buyAmountText = ""
                            },
                            enabled = isAmountValid && buyAmountDouble > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirmar compra", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = {
                                isExpanded = false
                                buyAmountText = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text("Cancelar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundApp)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
    }
}
