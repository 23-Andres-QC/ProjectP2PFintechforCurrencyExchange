package com.example.p2p.presentation.offer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.CreateOfferRequest
import com.example.p2p.ui.theme.*

// Todas las divisas que el backend soporta
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
    onNavigateBack: () -> Unit = {}
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

    // ── Estado local ──────────────────────────────────────────────────────────
    var amountText           by remember { mutableStateOf("") }
    var selectedSaleMode     by remember { mutableStateOf(0) }  // 0=Completa 1=Partes
    var selectedRateMode     by remember { mutableStateOf(0) }  // 0=Mercado 1=Rápida
    var customRateEnabled    by remember { mutableStateOf(false) }
    var customRateText       by remember { mutableStateOf("") }
    var minTransactionText   by remember { mutableStateOf("") }
    var maxTransactionText   by remember { mutableStateOf("") }

    var selectedCurrency     by remember { mutableStateOf("USD") }
    var expandedCurrency     by remember { mutableStateOf(false) }
    var selectedFiatCurrency by remember { mutableStateOf("PEN") }
    var expandedFiat         by remember { mutableStateOf(false) }

    // Cargar tasa real cuando cambia el par
    LaunchedEffect(selectedCurrency, selectedFiatCurrency) {
        viewModel?.loadExchangeRate(selectedCurrency, selectedFiatCurrency)
    }

    // Sincronizar customRateText con la tasa de mercado cuando llega
    LaunchedEffect(uiState.marketRate) {
        val rate = uiState.marketRate ?: return@LaunchedEffect
        if (!customRateEnabled) {
            customRateText = String.format("%.4f", rate)
        }
    }

    val symbol       = fiatSymbol(selectedFiatCurrency)
    val marketRate   = uiState.marketRate
    val quickRate    = marketRate?.let { it * 0.9950 } // 0.5% por debajo = venta rápida
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
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Par de Divisas ────────────────────────────────────────────────
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
                            .background(Primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
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
                // Indicador de tasa de referencia en tiempo real
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.06f))
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

            // ── Monto disponible ──────────────────────────────────────────────
            PublishSectionCard(title = "Monto Total Disponible") {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("Ej. 500.00 $selectedCurrency", color = TextMuted, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary, unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain, unfocusedTextColor = TextMain, cursorColor = Primary
                            )
                        )
                    }
                }
            }

            // ── Modo de Venta ─────────────────────────────────────────────────
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

            // ── Tasa de Cambio ────────────────────────────────────────────────
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

                    // Tasa personalizada
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

            // ── Vista Previa ──────────────────────────────────────────────────
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

            // ── Botón Publicar ────────────────────────────────────────────────
            Button(
                onClick = {
                    val minVal = if (selectedSaleMode == 0) amountDouble else (minTransactionText.toDoubleOrNull() ?: 50.0)
                    val maxVal = if (selectedSaleMode == 0) amountDouble else (maxTransactionText.toDoubleOrNull() ?: amountDouble)

                    when {
                        amountDouble <= 0 -> Toast.makeText(context, "Ingresa un monto válido.", Toast.LENGTH_SHORT).show()
                        currentRate <= 0  -> Toast.makeText(context, "La tasa de cambio no está disponible.", Toast.LENGTH_SHORT).show()
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
                                payment_methods = listOf("BCP")
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

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
        content()
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
