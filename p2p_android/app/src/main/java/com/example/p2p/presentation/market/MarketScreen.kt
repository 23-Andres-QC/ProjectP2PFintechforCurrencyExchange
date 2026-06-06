package com.example.p2p.presentation.market

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.ExchangeRate
import com.example.p2p.data.remote.model.Offer
import com.example.p2p.data.remote.model.Transaction
import com.example.p2p.ui.theme.*

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    userName: String = "Usuario",
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToTransaction: (String) -> Unit = {},
    onNavigateToAddBankAccount: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBuyDialog by remember { mutableStateOf<Offer?>(null) }
    val context = LocalContext.current

    val allCurrencies = listOf("PEN", "USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP")
    var selectedFiat     by remember { mutableStateOf("PEN") }
    var selectedCurrency by remember { mutableStateOf("USD") }

    LaunchedEffect(selectedFiat, selectedCurrency) {
        viewModel.loadOffers(currency = selectedCurrency, fiatCurrency = selectedFiat)
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadActiveTransactions()
            kotlinx.coroutines.delay(8000L)
        }
    }

    Scaffold(
        containerColor = BackgroundApp,
        topBar = {
            MarketTopBar(
                exchangeRates = uiState.exchangeRates,
                unreadCount = uiState.unreadCount,
                onNavigateToNotifications = {
                    viewModel.loadUnreadCount()
                    onNavigateToNotifications()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                FilterSection(
                    fiatOptions      = allCurrencies.filter { it != selectedCurrency },
                    currencyOptions  = allCurrencies.filter { it != selectedFiat },
                    selectedFiat     = selectedFiat,
                    selectedCurrency = selectedCurrency,
                    onFiatChange     = { selectedFiat = it },
                    onCurrencyChange = { selectedCurrency = it }
                )
            }

            if (uiState.activeTransactions.isNotEmpty()) {
                items(uiState.activeTransactions, key = { it.id }) { txn ->
                    ActiveTransactionBanner(
                        transaction = txn,
                        onClick = { onNavigateToTransaction(txn.id) }
                    )
                }
            }

            item {
                ActionRow(
                    isLoading = uiState.isLoading,
                    onMatchingClick = {
                        viewModel.matchOffer(
                            currency     = selectedCurrency,
                            fiatCurrency = selectedFiat,
                            onMatched    = { showBuyDialog = it },
                            onError      = { Toast.makeText(context, "Sin coincidencias: $it", Toast.LENGTH_SHORT).show() }
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

                uiState.offers.isEmpty() -> item {
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
                    val rateMap = uiState.exchangeRates.associateBy { "${it.from_currency}_${it.to_currency}" }
                    val marketRate = rateMap["${selectedCurrency}_${selectedFiat}"]?.rate
                    val isQuickSale: (Offer) -> Boolean = { offer ->
                        marketRate != null && offer.price_per_unit < marketRate
                    }
                    val sortedOffers = uiState.offers.sortedWith(compareByDescending { isQuickSale(it) })

                    item {
                        OffersHeader(count = uiState.offers.size, from = selectedCurrency, to = selectedFiat)
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

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
private fun MarketTopBar(
    exchangeRates: List<ExchangeRate> = emptyList(),
    unreadCount: Int = 0,
    onNavigateToNotifications: () -> Unit = {}
) {
    val targetPairs = listOf("USD", "EUR", "USDT", "COP", "MXN", "ARS")
    val rateMap = exchangeRates.associateBy { "${it.from_currency}_${it.to_currency}" }

    fun getRateToPen(from: String): Double? {
        rateMap["${from}_PEN"]?.let { return it.rate }
        val fromToUsd = rateMap["${from}_USD"]?.rate
        val usdToPen  = rateMap["USD_PEN"]?.rate
        if (fromToUsd != null && usdToPen != null) return fromToUsd * usdToPen
        return null
    }

    val tickerItems: List<Pair<String, String>> = run {
        val fromApi = targetPairs.mapNotNull { from ->
            val rate = getRateToPen(from) ?: return@mapNotNull null
            from to "S/${String.format("%.3f", rate)}"
        }
        fromApi.ifEmpty { listOf("USD" to "Cargando...", "EUR" to "Cargando...") }
    }

    Surface(color = Primary, shadowElevation = 6.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Peru", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Exchange", color = PrimaryMint, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNavigateToNotifications, modifier = Modifier.size(36.dp)) {
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tickerItems.forEach { (currency, rate) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(currency, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(rate, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("▲", color = PrimaryMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Filter Section ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    fiatOptions: List<String>,
    currencyOptions: List<String>,
    selectedFiat: String,
    selectedCurrency: String,
    onFiatChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit
) {
    Surface(color = SurfaceColor, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            Text("Filtrar:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            FilterDropdown(
                label = "Tengo",
                selected = selectedFiat,
                options = fiatOptions,
                onSelect = onFiatChange,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            FilterDropdown(
                label = "Quiero",
                selected = selectedCurrency,
                options = currencyOptions,
                onSelect = onCurrencyChange,
                modifier = Modifier.weight(1f)
            )
        }
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
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (expanded) Primary else BorderColor, RoundedCornerShape(8.dp))
                .background(BackgroundApp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontSize = 9.sp, color = TextMuted)
                Text(selected, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (expanded) Primary else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

// ─── Action Row ───────────────────────────────────────────────────────────────

@Composable
private fun ActionRow(isLoading: Boolean, onMatchingClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onMatchingClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningColor),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("Matching Automático", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
            Text("Mejor precio", fontSize = 11.sp, color = TextMuted)
        }
    }
}

// ─── Offers Header ────────────────────────────────────────────────────────────

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

// ─── Bank Account Selector ────────────────────────────────────────────────────

@Composable
fun BankAccountSelector(
    bankAccounts: List<BankAccount>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onNavigateToAddBankAccount: () -> Unit = {}
) {
    if (bankAccounts.isEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(WarningColor.copy(alpha = 0.08f))
                .border(1.dp, WarningColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable { onNavigateToAddBankAccount() }
                .padding(10.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Sin cuentas bancarias registradas.", fontSize = 11.sp, color = WarningColor, fontWeight = FontWeight.SemiBold)
                Text("Toca aquí para agregar una cuenta →", fontSize = 11.sp, color = WarningColor.copy(alpha = 0.8f))
            }
        }
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
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

// ─── Matching Dialog ──────────────────────────────────────────────────────────

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

// ─── Active Transaction Banner ───────────────────────────────────────────────

@Composable
private fun ActiveTransactionBanner(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val (statusLabel, statusColor, statusIcon) = when (transaction.status) {
        "pending"          -> Triple("Esperando al vendedor", WarningColor, Icons.Default.Schedule)
        "accepted"         -> Triple("Vendedor aceptó · Sube tu comprobante", SuccessColor, Icons.Default.CheckCircle)
        "voucher_uploaded" -> Triple("Verificando tu pago", Primary, Icons.Default.Pending)
        else               -> Triple("En curso", TextMuted, Icons.Default.Info)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(statusColor.copy(alpha = 0.09f))
            .border(1.5.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Transacción pendiente",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextMain
            )
            Text(
                text = statusLabel,
                fontSize = 11.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.2f", transaction.amount_from)} USD · S/ ${String.format("%.2f", transaction.amount_to)}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Offer Card ───────────────────────────────────────────────────────────────

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

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
                                Text("🏆", fontSize = 11.sp)
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
                                Text("⚡", fontSize = 11.sp)
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
                            Text("★ ${offer.vendor?.rating ?: 4.9}", fontSize = 11.sp, color = WarningColor, fontWeight = FontWeight.SemiBold)
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
                            onClick = {
                                val account = selectedAccount?.let { "${it.bank_name} · ${it.account_number}" } ?: "Mi cuenta"
                                onConfirmBuy(offer.available_amount, account)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Comprar todo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
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
}
