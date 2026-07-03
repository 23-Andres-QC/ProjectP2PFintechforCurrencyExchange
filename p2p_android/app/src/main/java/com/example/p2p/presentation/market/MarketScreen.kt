package com.example.p2p.presentation.market

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.R
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.ExchangeRate
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    userName: String = "Usuario",
    currentUserId: String = "",
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToAddBankAccount: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBuyDialog by remember { mutableStateOf<Offer?>(null) }
    val context = LocalContext.current

    val allCurrencies = listOf("PEN", "USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP")
    var selectedFiat     by remember { mutableStateOf("PEN") }
    var selectedCurrency by remember { mutableStateOf("USD") }

    val rateMap = uiState.exchangeRates.associateBy { "${it.from_currency}_${it.to_currency}" }
    val marketRate = rateMap["${selectedCurrency}_${selectedFiat}"]?.rate

    RefreshOnResume {
        viewModel.refreshHome(
            currency = selectedCurrency,
            fiatCurrency = selectedFiat,
            showLoading = false
        )
    }

    LaunchedEffect(selectedFiat, selectedCurrency) {
        viewModel.loadOffers(currency = selectedCurrency, fiatCurrency = selectedFiat)
        while (true) {
            kotlinx.coroutines.delay(4000L)
            viewModel.loadOffers(currency = selectedCurrency, fiatCurrency = selectedFiat, showLoading = false)
            viewModel.loadActiveTransactions()
            viewModel.loadUnreadCount()
        }
    }

    Scaffold(
        containerColor = BackgroundApp,
        topBar = {
            MarketTopBar(
                userName = userName,
                exchangeRates = uiState.exchangeRates,
                unreadCount = uiState.unreadCount,
                onNavigateToNotifications = {
                    viewModel.loadUnreadCount()
                    onNavigateToNotifications()
                },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                FilterAndMatchingRow(
                    fiatOptions      = allCurrencies.filter { it != selectedCurrency },
                    currencyOptions  = allCurrencies.filter { it != selectedFiat },
                    selectedFiat     = selectedFiat,
                    selectedCurrency = selectedCurrency,
                    onFiatChange     = { selectedFiat = it },
                    onCurrencyChange = { selectedCurrency = it },
                    onSwap           = {
                        val tmp = selectedFiat
                        selectedFiat = selectedCurrency
                        selectedCurrency = tmp
                    },
                    marketRate = marketRate,
                    isLoading = uiState.isLoading,
                    onMatchingClick = {
                        viewModel.matchOffer(
                            currency      = selectedCurrency,
                            fiatCurrency  = selectedFiat,
                            currentUserId = currentUserId,
                            onMatched     = { showBuyDialog = it },
                            onError       = { Toast.makeText(context, "Sin coincidencias: $it", Toast.LENGTH_SHORT).show() }
                        )
                    }
                )
            }

            when {
                uiState.isLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                            Text("Buscando ofertas...", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }

                uiState.error != null -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                                    .background(DangerColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, tint = DangerColor, modifier = Modifier.size(26.dp))
                            }
                            Text("No se pudo conectar", fontWeight = FontWeight.SemiBold, color = TextMain, fontSize = 14.sp)
                            Text("Verifica tu conexión e inténtalo de nuevo.", color = TextMuted, fontSize = 12.sp)
                            Button(
                                onClick = { viewModel.loadOffers(selectedCurrency, selectedFiat) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.offers.filter { it.vendor_id != currentUserId }.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = Primary, modifier = Modifier.size(30.dp))
                            }
                            Text("Sin ofertas disponibles", fontWeight = FontWeight.SemiBold, color = TextMain, fontSize = 14.sp)
                            Text("No hay ofertas de $selectedCurrency → $selectedFiat ahora.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                else -> {
                    val isQuickSale: (Offer) -> Boolean = { offer ->
                        marketRate != null && offer.price_per_unit < marketRate
                    }
                    val visibleOffers = uiState.offers.filter { it.vendor_id != currentUserId }
                    val sortedOffers = visibleOffers.sortedWith(compareByDescending { isQuickSale(it) })

                    item {
                        OffersHeader(count = visibleOffers.size, from = selectedCurrency, to = selectedFiat)
                    }
                    itemsIndexed(sortedOffers, key = { _, o -> o.id }) { index, offer ->
                        OfferCard(
                            offer = offer,
                            isBestRate = index == 0,
                            isQuickSale = isQuickSale(offer),
                            bankAccounts = uiState.bankAccounts,
                            selectedBankAccountId = uiState.selectedBankAccountId,
                            onSelectBankAccount = { viewModel.selectBankAccount(it) },
                            onNavigateToAddBankAccount = onNavigateToAddBankAccount,
                            onConfirmBuy = { amount, buyerAccount ->
                                val req = CreateTransactionRequest(
                                    offer_id = offer.id,
                                    amount_from = amount,
                                    amount_to = amount * offer.price_per_unit,
                                    buyer_payment_account = buyerAccount,
                                    vendor_payment_account = offer.payment_methods?.firstOrNull() ?: "BCP"
                                )
                                viewModel.createTransaction(req,
                                    onSuccess = { txnId ->
                                        Toast.makeText(context, "¡Compra iniciada! El vendedor fue notificado.", Toast.LENGTH_SHORT).show()
                                        onNavigateToTransaction(txnId)
                                    },
                                    onError   = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    showBuyDialog?.let { offer ->
        MatchingDialog(
            offer = offer,
            bankAccounts = uiState.bankAccounts,
            selectedBankAccountId = uiState.selectedBankAccountId,
            onSelectBankAccount = { viewModel.selectBankAccount(it) },
            onNavigateToAddBankAccount = onNavigateToAddBankAccount,
            onConfirm = { amount, buyerAccount ->
                val req = CreateTransactionRequest(
                    offer_id = offer.id,
                    amount_from = amount,
                    amount_to = amount * offer.price_per_unit,
                    buyer_payment_account = buyerAccount,
                    vendor_payment_account = offer.payment_methods?.firstOrNull() ?: "BCP"
                )
                viewModel.createTransaction(req,
                    onSuccess = { txnId ->
                        showBuyDialog = null
                        Toast.makeText(context, "¡Compra iniciada! El vendedor fue notificado.", Toast.LENGTH_SHORT).show()
                        onNavigateToTransaction(txnId)
                    },
                    onError   = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                )
            },
            onDismiss = { showBuyDialog = null }
        )
    }
}

@Composable
private fun MarketTopBar(
    userName: String = "Usuario",
    exchangeRates: List<ExchangeRate> = emptyList(),
    unreadCount: Int = 0,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val initials = userName.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

    val tickerItems: List<Pair<String, String>> = run {
        val rateMap = exchangeRates.associateBy { "${it.from_currency}_${it.to_currency}" }
        val targetPairs = listOf("USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP")

        fun rateToPen(from: String): Double? {
            rateMap["${from}_PEN"]?.let { return it.rate }
            rateMap["PEN_${from}"]?.rate?.takeIf { it != 0.0 }?.let { return 1.0 / it }
            val fromToUsd = rateMap["${from}_USD"]?.rate
            val usdToPen = rateMap["USD_PEN"]?.rate
            if (fromToUsd != null && usdToPen != null) return fromToUsd * usdToPen
            val usdToFrom = rateMap["USD_${from}"]?.rate
            if (usdToFrom != null && usdToFrom != 0.0 && usdToPen != null) return usdToPen / usdToFrom
            return null
        }

        val fromApi = exchangeRates
            .map { it.from_currency }
            .plus(targetPairs)
            .distinct()
            .filter { it != "PEN" }
            .mapNotNull { from ->
                val rate = rateToPen(from) ?: return@mapNotNull null
                "$from/PEN" to "S/${formatTickerNumber(rate)}"
            }
        fromApi.ifEmpty {
            listOf(
                "USD/PEN" to "Cargando...",
                "EUR/PEN" to "Cargando...",
                "USDT/PEN" to "Cargando..."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(Brush.verticalGradient(listOf(Primary, PrimaryDark)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_market_logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text("Peru", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Exchange", color = PrimaryMint, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.12f))))
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onNavigateToNotifications, modifier = Modifier.size(38.dp)) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = DangerColor, contentColor = Color.White) {
                                    Text(
                                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        ExchangeRateMarquee(
            tickerItems = tickerItems,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun ExchangeRateMarquee(
    tickerItems: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val virtualCount = 100_000
    val startIndex = remember(tickerItems) {
        val middle = virtualCount / 2
        middle - (middle % tickerItems.size.coerceAtLeast(1))
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    LaunchedEffect(tickerItems) {
        if (tickerItems.isEmpty()) return@LaunchedEffect
        while (true) {
            listState.scrollBy(1.2f)
            delay(16L)
            if (listState.firstVisibleItemIndex < 1_000 || listState.firstVisibleItemIndex > virtualCount - 1_000) {
                listState.scrollToItem(startIndex, listState.firstVisibleItemScrollOffset)
            }
        }
    }

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(50.dp))
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(virtualCount) { index ->
                val (pair, rate) = tickerItems[index % tickerItems.size]
                ExchangeRatePill(pair = pair, rate = rate)
            }
        }
    }
}

@Composable
private fun ExchangeRatePill(pair: String, rate: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(PrimaryMint)
        )
        Text(pair, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(rate, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
    }
}

private fun formatTickerRate(rate: ExchangeRate): String {
    val value = formatTickerNumber(rate.rate)
    return if (rate.to_currency == "PEN") "S/$value" else value
}

private fun formatTickerNumber(value: Double): String = when {
    value >= 100 -> String.format("%.2f", value)
    value >= 1 -> String.format("%.3f", value)
    else -> String.format("%.5f", value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterAndMatchingRow(
    fiatOptions: List<String>,
    currencyOptions: List<String>,
    selectedFiat: String,
    selectedCurrency: String,
    onFiatChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onSwap: () -> Unit,
    marketRate: Double?,
    isLoading: Boolean,
    onMatchingClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        elevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text("$selectedCurrency → $selectedFiat", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Text(
                    marketRate?.let { "$selectedFiat ${String.format("%.3f", it)}" } ?: "—",
                    fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, maxLines = 1
                )
            }

            TextButton(
                onClick = { showDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filtro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onMatchingClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarningColor),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Matching", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    "Filtrar por moneda",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = TextMain,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterDropdown(
                        label = "Tengo",
                        selected = selectedFiat,
                        options = fiatOptions,
                        onSelect = onFiatChange,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Intercambiar", tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    FilterDropdown(
                        label = "Quiero",
                        selected = selectedCurrency,
                        options = currencyOptions,
                        onSelect = onCurrencyChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Aplicar filtro", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, if (expanded) Primary else Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.05f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Text(selected, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (expanded) Primary else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = SurfaceColor,
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.widthIn(min = 176.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    opt,
                                    fontWeight = if (opt == selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (opt == selected) Primary else TextMain
                                )
                            },
                            onClick = { onSelect(opt); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OffersHeader(count: Int, from: String, to: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ofertas $from → $to", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Primary)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$count", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BankAccountSelector(
    bankAccounts: List<BankAccount>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onNavigateToAddBankAccount: () -> Unit = {}
) {
    // El comprador no necesita tener una cuenta bancaria registrada (eso solo aplica
    // al vendedor al publicar, que es donde se deposita el dinero). Si no tiene ninguna,
    // simplemente no se muestra selector ni aviso: la compra sigue funcionando igual.
    if (bankAccounts.isEmpty()) {
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val selected = bankAccounts.firstOrNull { it.id == selectedId } ?: bankAccounts.first()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Pagar desde mi cuenta:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.04f))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(selected.bank_name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    Text("${selected.account_number} · ${selected.currency}", fontSize = 11.sp, color = TextMuted)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = SurfaceColor
            ) {
                bankAccounts.forEach { account ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(account.bank_name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${account.account_number} · ${account.currency}", fontSize = 11.sp, color = TextMuted)
                            }
                        },
                        onClick = { onSelect(account.id); expanded = false },
                        leadingIcon = if (account.id == selectedId) ({
                            Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        }) else null
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchingDialog(
    offer: Offer,
    bankAccounts: List<BankAccount>,
    selectedBankAccountId: String?,
    onSelectBankAccount: (String) -> Unit,
    onNavigateToAddBankAccount: () -> Unit = {},
    onConfirm: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isPartial = offer.offer_type == "partial"
    var amountText by remember(offer.id) {
        mutableStateOf(if (isPartial) "" else offer.available_amount.toString())
    }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val selectedAccount = bankAccounts.firstOrNull { it.id == selectedBankAccountId } ?: bankAccounts.firstOrNull()

    val isAmountValid = if (isPartial) {
        amount >= offer.min_transaction &&
            amount <= (offer.max_transaction ?: offer.available_amount) &&
            amount <= offer.available_amount
    } else {
        amount == offer.available_amount
    }
    val canConfirm = isAmountValid && (selectedAccount != null || bankAccounts.isEmpty())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = WarningColor)
                Text("Matching: Comprar ${offer.currency}")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Vendedor: ${offer.vendor?.full_name ?: "Vendedor"}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Tasa: ${offer.fiat_currency} ${String.format("%.3f", offer.price_per_unit)}", fontSize = 14.sp)
                Text("Disponible: ${offer.available_amount} ${offer.currency}", fontSize = 13.sp)

                if (isPartial) {
                    Text("Límites: ${offer.min_transaction} – ${offer.max_transaction ?: offer.available_amount} ${offer.currency}", fontSize = 12.sp, color = TextMuted)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto a comprar (${offer.currency})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor, focusedTextColor = TextMain, cursorColor = Primary)
                    )
                }

                if (amount > 0 && isAmountValid) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(SuccessColor.copy(alpha = 0.08f)).padding(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Pagarás aprox.", fontSize = 11.sp, color = TextMuted)
                                Text("${offer.fiat_currency} ${String.format("%.2f", amount * offer.price_per_unit)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Primary)
                        }
                    }
                }

                BankAccountSelector(
                    bankAccounts = bankAccounts,
                    selectedId = selectedBankAccountId,
                    onSelect = onSelectBankAccount,
                    onNavigateToAddBankAccount = onNavigateToAddBankAccount
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val account = selectedAccount?.let { "${it.bank_name} · ${it.account_number}" } ?: "Mi cuenta"
                    onConfirm(amount, account)
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
            ) {
                Text("Confirmar compra")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun OfferCard(
    offer: Offer,
    isBestRate: Boolean = false,
    isQuickSale: Boolean = false,
    bankAccounts: List<BankAccount>,
    selectedBankAccountId: String?,
    onSelectBankAccount: (String) -> Unit,
    onNavigateToAddBankAccount: () -> Unit = {},
    onConfirmBuy: (Double, String) -> Unit
) {
    val isPartial = offer.offer_type == "partial"
    var isExpanded by remember { mutableStateOf(false) }
    var buyAmountText by remember { mutableStateOf("") }
    var showBuyAllDialog by remember { mutableStateOf(false) }
    val buyAmount = buyAmountText.toDoubleOrNull() ?: 0.0

    val isAmountValid = if (isPartial) {
        buyAmount >= offer.min_transaction &&
            buyAmount <= (offer.max_transaction ?: offer.available_amount) &&
            buyAmount <= offer.available_amount
    } else true

    val selectedAccount = bankAccounts.firstOrNull { it.id == selectedBankAccountId } ?: bankAccounts.firstOrNull()
    val initials = offer.vendor?.full_name?.trim()?.split(" ")
        ?.filter { it.isNotEmpty() }?.take(2)?.map { it.first().uppercaseChar() }?.joinToString("") ?: "??"
    val verified = offer.vendor?.kyc_verified ?: false

    GlassCard(
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isBestRate || isQuickSale) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isBestRate) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarningColor.copy(alpha = 0.12f))
                                    .border(1.dp, WarningColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = WarningColor, modifier = Modifier.size(12.dp))
                                Text("Mejor tasa del mercado", color = WarningColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (isQuickSale) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DangerColor.copy(alpha = 0.1f))
                                    .border(1.dp, DangerColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = DangerColor, modifier = Modifier.size(12.dp))
                                Text("Venta rápida", color = DangerColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                offer.vendor?.full_name ?: "Vendedor",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain
                            )
                            if (verified) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = WarningColor, modifier = Modifier.size(12.dp))
                                Text("${offer.vendor?.rating ?: 4.9}", fontSize = 11.sp, color = WarningColor, fontWeight = FontWeight.SemiBold)
                            }
                            Text("${offer.vendor?.total_transactions ?: 0} ops", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(SuccessColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("En línea", color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Tasa", fontSize = 10.sp, color = TextMuted)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPartial) Primary.copy(alpha = 0.1f) else SuccessColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (isPartial) "PARCIAL" else "COMPLETA",
                                    color = if (isPartial) Primary else SuccessColor,
                                    fontSize = 8.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            "${offer.fiat_currency} ${String.format("%.3f", offer.price_per_unit)}",
                            fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "${String.format("%.2f", offer.available_amount)} ${offer.currency}",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain
                        )
                        Text("disponibles", fontSize = 10.sp, color = TextMuted)
                        if (isPartial) {
                            Text(
                                "Rango: ${offer.min_transaction.toInt()} – ${(offer.max_transaction ?: offer.available_amount).toInt()} ${offer.currency}",
                                fontSize = 10.sp, color = TextMuted
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        offer.payment_methods?.take(2)?.forEach { method ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Primary.copy(alpha = 0.08f))
                                    .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(method, color = Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (isPartial) {
                        Button(
                            onClick = { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isExpanded) "Cerrar" else "Elegir monto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { showBuyAllDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Comprar todo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (showBuyAllDialog && !isPartial) {
                    AlertDialog(
                        onDismissRequest = { showBuyAllDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SuccessColor)
                                Text("Confirmar compra")
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Vendedor: ${offer.vendor?.full_name ?: "Vendedor"}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tasa: ${offer.fiat_currency} ${String.format("%.3f", offer.price_per_unit)}",
                                    fontSize = 13.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SuccessColor.copy(alpha = 0.08f))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Recibirás:", fontSize = 12.sp, color = TextMuted)
                                            Text(
                                                "${String.format("%.2f", offer.available_amount)} ${offer.currency}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextMain
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Pagarás:", fontSize = 12.sp, color = TextMuted)
                                            Text(
                                                "${offer.fiat_currency} ${String.format("%.2f", offer.available_amount * offer.price_per_unit)}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SuccessColor
                                            )
                                        }
                                    }
                                }
                                BankAccountSelector(
                                    bankAccounts = bankAccounts,
                                    selectedId = selectedBankAccountId,
                                    onSelect = onSelectBankAccount,
                                    onNavigateToAddBankAccount = onNavigateToAddBankAccount
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showBuyAllDialog = false
                                    val account = selectedAccount?.let { "${it.bank_name} · ${it.account_number}" } ?: "Mi cuenta"
                                    onConfirmBuy(offer.available_amount, account)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                            ) {
                                Text("Confirmar compra", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBuyAllDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (isExpanded && isPartial) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundApp)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("¿Cuánto deseas comprar?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextMain)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = buyAmountText,
                                onValueChange = { buyAmountText = it },
                                placeholder = { Text("0.00", fontSize = 13.sp, color = TextMuted) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Primary
                                )
                            )
                            Text(offer.currency, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        if (buyAmount > 0) {
                            if (isAmountValid) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessColor.copy(alpha = 0.06f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Pagarás aprox.", fontSize = 10.sp, color = TextMuted)
                                        Text(
                                            "${offer.fiat_currency} ${String.format("%.2f", buyAmount * offer.price_per_unit)}",
                                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SuccessColor
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                when {
                                    buyAmount < offer.min_transaction ->
                                        Text("El mínimo es ${offer.min_transaction} ${offer.currency}", color = DangerColor, fontSize = 11.sp)
                                    offer.max_transaction != null && buyAmount > offer.max_transaction ->
                                        Text("El máximo es ${offer.max_transaction} ${offer.currency}", color = DangerColor, fontSize = 11.sp)
                                    buyAmount > offer.available_amount ->
                                        Text("Solo hay ${offer.available_amount} ${offer.currency} disponibles", color = DangerColor, fontSize = 11.sp)
                                }
                            }
                        }

                        BankAccountSelector(
                            bankAccounts = bankAccounts,
                            selectedId = selectedBankAccountId,
                            onSelect = onSelectBankAccount,
                            onNavigateToAddBankAccount = onNavigateToAddBankAccount
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val account = selectedAccount?.let { "${it.bank_name} · ${it.account_number}" } ?: "Mi cuenta"
                                    onConfirmBuy(buyAmount, account)
                                    isExpanded = false
                                    buyAmountText = ""
                                },
                                enabled = isAmountValid && buyAmount > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirmar compra", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { isExpanded = false; buyAmountText = "" },
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
