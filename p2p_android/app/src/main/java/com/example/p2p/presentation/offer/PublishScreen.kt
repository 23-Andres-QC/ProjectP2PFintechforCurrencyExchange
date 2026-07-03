package com.example.p2p.presentation.offer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateOfferRequest
import androidx.compose.ui.graphics.Brush
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.theme.*

private val ALL_CURRENCIES = listOf("PEN", "USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP")

private fun fiatSymbol(currency: String) = when (currency) {
    "PEN"  -> "S/"
    "COP"  -> "COP"
    "MXN"  -> "MX\$"
    "ARS"  -> "AR\$"
    "CLP"  -> "CLP"
    "BRL"  -> "R\$"
    "USD"  -> "US\$"
    "EUR"  -> "€"
    else   -> currency
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    viewModel: PublishViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToBankAccounts: () -> Unit = {}
) {
    val uiState by viewModel?.uiState?.collectAsState(initial = PublishUiState())
        ?: remember { mutableStateOf(PublishUiState()) }
    val context = LocalContext.current

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, "Oferta publicada con éxito", Toast.LENGTH_SHORT).show()
            viewModel?.resetState()
            onNavigateBack()
        }
    }
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel?.resetState()
        }
    }

    var currentStep          by remember { mutableStateOf(1) }
    var amountText           by remember { mutableStateOf("") }
    var selectedSaleMode     by remember { mutableStateOf(0) }
    var selectedRateMode     by remember { mutableStateOf(0) }
    var customRateEnabled    by remember { mutableStateOf(false) }
    var customRateText       by remember { mutableStateOf("") }
    var minTransactionText   by remember { mutableStateOf("") }
    var maxTransactionText   by remember { mutableStateOf("") }
    var selectedAccountId    by remember { mutableStateOf<String?>(null) }

    var selectedCurrency     by remember { mutableStateOf("USD") }
    var expandedCurrency     by remember { mutableStateOf(false) }
    var selectedFiatCurrency by remember { mutableStateOf("PEN") }
    var expandedFiat         by remember { mutableStateOf(false) }

    val accountsForFiat = uiState.bankAccounts.filter { it.currency == selectedFiatCurrency }
    val selectedAccount = accountsForFiat.find { it.id == selectedAccountId }
        ?: accountsForFiat.find { it.is_primary }
        ?: accountsForFiat.firstOrNull()

    RefreshOnResume {
        viewModel?.loadBankAccounts()
        viewModel?.loadExchangeRate(selectedCurrency, selectedFiatCurrency)
    }

    LaunchedEffect(selectedFiatCurrency) { selectedAccountId = null }

    LaunchedEffect(selectedCurrency, selectedFiatCurrency) {
        viewModel?.loadExchangeRate(selectedCurrency, selectedFiatCurrency)
    }

    LaunchedEffect(uiState.marketRate) {
        val rate = uiState.marketRate ?: return@LaunchedEffect
        if (!customRateEnabled) {
            customRateText = String.format("%.4f", rate)
        }
    }

    val symbol       = fiatSymbol(selectedFiatCurrency)
    val marketRate   = uiState.marketRate
    val quickRate    = marketRate?.let { it * 0.9950 }
    val amountDouble = amountText.toDoubleOrNull() ?: 0.0

    val currentRate = when {
        customRateEnabled -> customRateText.toDoubleOrNull() ?: marketRate ?: 0.0
        selectedRateMode == 1 -> quickRate ?: marketRate ?: 0.0
        else -> marketRate ?: 0.0
    }

    val amountToReceive = amountDouble * currentRate

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Publicar Anuncio P2P", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextMain)
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep == 1) onNavigateBack() else currentStep = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        containerColor = BackgroundApp
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.08f), BackgroundApp)))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                elevation = 4.dp,
            ) {
                PublishStepIndicator(currentStep = currentStep)
            }

            if (currentStep == 1) {
                PublishSectionCard(title = "Par de Divisas") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyDropdown(
                            label = "Ofrezco",
                            selected = selectedCurrency,
                            options = ALL_CURRENCIES.filter { it != selectedFiatCurrency },
                            expanded = expandedCurrency,
                            onExpandChange = { expandedCurrency = !expandedCurrency },
                            onSelect = { selectedCurrency = it; expandedCurrency = false },
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Primary.copy(alpha = 0.10f))
                                .clickable {
                                    val tmp = selectedCurrency
                                    selectedCurrency = selectedFiatCurrency
                                    selectedFiatCurrency = tmp
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "Intercambiar", tint = Primary, modifier = Modifier.size(22.dp))
                        }
                        CurrencyDropdown(
                            label = "Recibo en",
                            selected = selectedFiatCurrency,
                            options = ALL_CURRENCIES.filter { it != selectedCurrency },
                            expanded = expandedFiat,
                            onExpandChange = { expandedFiat = !expandedFiat },
                            onSelect = { selectedFiatCurrency = it; expandedFiat = false },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tipo de cambio referencial:", fontSize = 12.sp, color = TextMuted)
                        when {
                            uiState.isLoadingRate -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Primary)
                                    Text("Consultando API...", fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            marketRate != null -> Text(
                                "$symbol ${String.format("%.4f", marketRate)}",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessColor
                            )
                            else -> Text("No disponible", fontSize = 12.sp, color = DangerColor)
                        }
                    }
                }

                PublishSectionCard(title = "Cuenta donde recibirás el pago") {
                    if (uiState.isLoadingAccounts) {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
                        }
                    } else if (accountsForFiat.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningColor.copy(alpha = 0.07f))
                                .border(1.dp, WarningColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "No tienes cuentas en $selectedFiatCurrency",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningColor
                                )
                            }
                            Text(
                                text = "Debes agregar una cuenta bancaria en $selectedFiatCurrency para recibir el pago de tus compradores.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Button(
                                onClick = onNavigateToBankAccounts,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregar cuenta $selectedFiatCurrency", fontSize = 13.sp, color = Color.White)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            accountsForFiat.forEach { account ->
                                val isSelected = account.id == (selectedAccount?.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Primary.copy(alpha = 0.07f) else SurfaceColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Primary else BorderColor,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedAccountId = account.id }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalance,
                                        contentDescription = null,
                                        tint = if (isSelected) Primary else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = account.bank_name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Primary else TextMain
                                        )
                                        Text(
                                            text = account.account_number,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                        if (!account.account_holder.isNullOrBlank()) {
                                            Text(
                                                text = account.account_holder,
                                                fontSize = 11.sp,
                                                color = TextMuted.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { currentStep = 2 },
                    enabled = selectedAccount != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Continuar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                if (selectedAccount == null) {
                    Text(
                        "Selecciona o agrega una cuenta para continuar",
                        fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.06f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("$selectedCurrency → $selectedFiatCurrency", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text(selectedAccount?.bank_name ?: "", fontSize = 11.sp, color = TextMuted)
                    }
                    TextButton(onClick = { currentStep = 1 }) {
                        Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                    }
                }

                PublishSectionCard(title = "Monto Total Disponible") {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        placeholder = { Text("Ej. 500.00 $selectedCurrency", color = TextMuted, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Primary
                        )
                    )
                    if (selectedSaleMode == 1) {
                        Spacer(Modifier.height(4.dp))
                        Text("Límites por transacción (Opcional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = minTransactionText,
                                onValueChange = { minTransactionText = it },
                                placeholder = { Text("Mínimo", color = TextMuted, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Primary
                                )
                            )
                            OutlinedTextField(
                                value = maxTransactionText,
                                onValueChange = { maxTransactionText = it },
                                placeholder = { Text("Máximo", color = TextMuted, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Primary
                                )
                            )
                        }
                    }
                }

                PublishSectionCard(title = "Modo de Venta") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SaleModeOption(
                            modifier = Modifier.weight(1f),
                            title = "Venta Completa",
                            description = "Vendo todo el monto de una vez",
                            iconVector = Icons.Filled.MonetizationOn,
                            isSelected = selectedSaleMode == 0,
                            onClick = { selectedSaleMode = 0 }
                        )
                        SaleModeOption(
                            modifier = Modifier.weight(1f),
                            title = "Venta por Partes",
                            description = "El comprador elige cuánto compra",
                            iconVector = Icons.Filled.Extension,
                            isSelected = selectedSaleMode == 1,
                            onClick = { selectedSaleMode = 1 }
                        )
                    }
                }

                PublishSectionCard(title = "Tasa de Cambio") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            RateOption(
                                modifier = Modifier.weight(1f),
                                label = "Tasa Mercado",
                                rateText = when {
                                    uiState.isLoadingRate -> "Cargando..."
                                    marketRate != null    -> "$symbol ${String.format("%.4f", marketRate)}"
                                    else                  -> "No disponible"
                                },
                                rateColor = if (marketRate != null) SuccessColor else TextMuted,
                                subtitle = "Tasa real (API)",
                                isSelected = selectedRateMode == 0 && !customRateEnabled,
                                onClick = { selectedRateMode = 0; customRateEnabled = false }
                            )
                            RateOption(
                                modifier = Modifier.weight(1f),
                                label = "Venta Rápida",
                                rateText = when {
                                    uiState.isLoadingRate -> "Cargando..."
                                    quickRate != null     -> "$symbol ${String.format("%.4f", quickRate)}"
                                    else                  -> "No disponible"
                                },
                                rateColor = if (quickRate != null) WarningColor else TextMuted,
                                subtitle = "0.5% bajo mercado",
                                isSelected = selectedRateMode == 1 && !customRateEnabled,
                                onClick = { selectedRateMode = 1; customRateEnabled = false }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BackgroundApp)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Switch(
                                checked = customRateEnabled,
                                onCheckedChange = {
                                    customRateEnabled = it
                                    if (it && customRateText.isEmpty()) {
                                        customRateText = String.format("%.4f", marketRate ?: 0.0)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SurfaceColor, checkedTrackColor = Primary,
                                    uncheckedThumbColor = SurfaceColor, uncheckedTrackColor = BorderColor
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                            Text("Tasa personalizada", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMain, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = customRateText,
                                onValueChange = { customRateText = it },
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = customRateEnabled,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (customRateEnabled) TextMain else TextMuted
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                    disabledBorderColor = BorderColor, focusedTextColor = TextMain,
                                    unfocusedTextColor = TextMuted, cursorColor = Primary
                                )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary.copy(alpha = 0.06f))
                        .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Vista Previa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
                    PreviewRow("Par:", "$selectedCurrency → $selectedFiatCurrency", Primary)
                    PreviewRow(
                        "Tasa aplicada:",
                        if (currentRate > 0) "$symbol ${String.format("%.4f", currentRate)}" else "—",
                        if (currentRate > 0) TextMain else TextMuted
                    )
                    if (amountDouble > 0 && currentRate > 0) {
                        PreviewRow(
                            "Recibirás aprox:",
                            "$symbol ${String.format("%.2f", amountToReceive)}",
                            SuccessColor
                        )
                    }
                    if (marketRate != null && currentRate > 0) {
                        val diff = ((currentRate - marketRate) / marketRate) * 100
                        val diffText = if (diff >= 0) "+${String.format("%.2f", diff)}%" else "${String.format("%.2f", diff)}%"
                        val diffColor = if (diff >= 0) SuccessColor else DangerColor
                        PreviewRow("vs. Mercado:", diffText, diffColor)
                    }
                }

                Button(
                    onClick = {
                        val minVal = if (selectedSaleMode == 0) amountDouble else (minTransactionText.toDoubleOrNull() ?: 50.0)
                        val maxVal = if (selectedSaleMode == 0) amountDouble else (maxTransactionText.toDoubleOrNull() ?: amountDouble)

                        when {
                            amountDouble <= 0 -> Toast.makeText(context, "Ingresa un monto válido.", Toast.LENGTH_SHORT).show()
                            currentRate <= 0  -> Toast.makeText(context, "La tasa de cambio no está disponible.", Toast.LENGTH_SHORT).show()
                            selectedAccount == null -> Toast.makeText(context, "Agrega una cuenta bancaria en $selectedFiatCurrency para recibir el pago.", Toast.LENGTH_LONG).show()
                            selectedSaleMode == 1 && minVal > maxVal ->
                                Toast.makeText(context, "El mínimo no puede ser mayor al máximo.", Toast.LENGTH_SHORT).show()
                            selectedSaleMode == 1 && maxVal > amountDouble ->
                                Toast.makeText(context, "El máximo no puede superar el monto disponible.", Toast.LENGTH_SHORT).show()
                            else -> viewModel?.publishOffer(
                                CreateOfferRequest(
                                    currency = selectedCurrency,
                                    fiat_currency = selectedFiatCurrency,
                                    amount = amountDouble,
                                    price_per_unit = currentRate,
                                    offer_type = if (selectedSaleMode == 0) "full" else "partial",
                                    min_transaction = minVal,
                                    max_transaction = maxVal,
                                    payment_methods = listOf("${selectedAccount!!.bank_name} · ${selectedAccount.account_number}")
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !uiState.isLoading && amountText.isNotEmpty() && currentRate > 0
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publicar Anuncio", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PublishStepIndicator(currentStep: Int) {
    val steps = listOf("Cuenta" to Icons.Filled.AccountBalance, "Oferta" to Icons.Filled.Campaign)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, (label, icon) ->
            val step = index + 1
            val isDone = step < currentStep
            val isCurrent = step == currentStep
            val color = when { isDone -> SuccessColor; isCurrent -> Primary; else -> BorderColor }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(when { isDone -> SuccessColor; isCurrent -> Primary; else -> SurfaceColor })
                        .border(2.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(icon, contentDescription = null, tint = if (isCurrent) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = color
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .height(2.dp)
                        .background(if (step < currentStep) SuccessColor else BorderColor)
                )
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandChange() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, BorderColor, RoundedCornerShape(10.dp))
                    .background(SurfaceColor)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selected, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                    Text("▾", fontSize = 12.sp, color = TextMuted)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onSelect(selected) }) {
                options.forEach { cur ->
                    DropdownMenuItem(
                        text = { Text(cur, fontWeight = if (cur == selected) FontWeight.Bold else FontWeight.Normal, color = if (cur == selected) Primary else TextMain) },
                        onClick = { onSelect(cur) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PublishSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
            content()
        }
    }
}

@Composable
private fun SaleModeOption(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    iconVector: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.06f) else SurfaceColor)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Primary else BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(iconVector, contentDescription = null, tint = if (isSelected) Primary else TextMuted, modifier = Modifier.size(24.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Primary else TextMain)
        Text(description, fontSize = 10.sp, color = TextMuted, lineHeight = 13.sp)
    }
}

@Composable
private fun RateOption(
    modifier: Modifier = Modifier,
    label: String,
    rateText: String,
    rateColor: Color,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.06f) else SurfaceColor)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Primary else BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) Primary else TextMuted)
        Text(rateText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rateColor)
        Text(subtitle, fontSize = 10.sp, color = TextMuted)
    }
}
