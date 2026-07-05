package com.example.p2p.presentation.bank_accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.components.GlassCard
import com.example.p2p.ui.components.GlassIconBadge
import com.example.p2p.ui.theme.*

// ── Códigos de país ──────────────────────────────────────────────────────────

private data class CountryCode(
    val flag: String,
    val name: String,
    val dialCode: String
)

private val COUNTRY_CODES = listOf(
    CountryCode("🇵🇪", "Perú",      "+51"),
    CountryCode("🇺🇸", "EE.UU.",    "+1"),
    CountryCode("🇨🇴", "Colombia",  "+57"),
    CountryCode("🇦🇷", "Argentina", "+54"),
    CountryCode("🇲🇽", "México",    "+52"),
    CountryCode("🇨🇱", "Chile",     "+56"),
    CountryCode("🇧🇷", "Brasil",    "+55"),
    CountryCode("🇪🇨", "Ecuador",   "+593"),
    CountryCode("🇻🇪", "Venezuela", "+58"),
    CountryCode("🇧🇴", "Bolivia",   "+591"),
)

// ── Definición de bancos ─────────────────────────────────────────────────────

private data class BankDef(
    val key: String,
    val displayName: String,
    val shortHint: String,
    val fieldLabel: String,
    val fieldHint: String,
    val maxLength: Int,
    val digitsOnly: Boolean,
    val color: Color,
    val currencies: List<String>,   // monedas soportadas por este banco
    val isOther: Boolean = false
)

private val BANKS = listOf(
    BankDef(
        key = "BCP", displayName = "BCP", shortHint = "CCI · 20 díg.",
        fieldLabel = "Número de CCI",
        fieldHint = "20 dígitos · ej. 00219100987654321234",
        maxLength = 20, digitsOnly = true, color = BcpColor,
        currencies = listOf("PEN", "USD")           // BCP: soles y dólares
    ),
    BankDef(
        key = "Interbank", displayName = "Interbank", shortHint = "CCI · 20 díg.",
        fieldLabel = "Número de CCI",
        fieldHint = "20 dígitos · ej. 00385800987654321234",
        maxLength = 20, digitsOnly = true, color = InterbankColor,
        currencies = listOf("PEN", "USD", "EUR")    // Interbank: soles, dólares y euros
    ),
    BankDef(
        key = "BBVA", displayName = "BBVA", shortHint = "CCI · 20 díg.",
        fieldLabel = "Número de CCI",
        fieldHint = "20 dígitos · ej. 01140024987654321234",
        maxLength = 20, digitsOnly = true, color = BbvaColor,
        currencies = listOf("PEN", "USD", "EUR")    // BBVA: soles, dólares y euros
    ),
    BankDef(
        key = "Yape", displayName = "Yape", shortHint = "Celular · 9 díg.",
        fieldLabel = "Número de celular",
        fieldHint = "9 dígitos · ej. 987654321",
        maxLength = 9, digitsOnly = true, color = YapeColor,
        currencies = listOf("PEN")                  // Yape: solo soles
    ),
    BankDef(
        key = "Plin", displayName = "Plin", shortHint = "Celular · 9 díg.",
        fieldLabel = "Número de celular",
        fieldHint = "9 dígitos · ej. 987654321",
        maxLength = 9, digitsOnly = true, color = PlinColor,
        currencies = listOf("PEN")                  // Plin: solo soles
    ),
    BankDef(
        key = "Otros", displayName = "Otros", shortHint = "Libre",
        fieldLabel = "Número / Código de cuenta",
        fieldHint = "IBAN, número de cuenta, wallet, etc.",
        maxLength = 60, digitsOnly = false,
        color = Color(0xFF64748B), isOther = true,
        currencies = listOf("PEN", "USD", "EUR", "USDT", "GBP", "BRL", "COP", "MXN")
    ),
)

private fun validate(value: String, bank: BankDef): String? {
    if (value.isBlank()) return "El número de cuenta es obligatorio"
    return when {
        bank.isOther -> if (value.trim().length < 4) "Mínimo 4 caracteres" else null
        bank.digitsOnly -> when {
            !value.all { it.isDigit() } -> "Solo se permiten dígitos"
            value.length < bank.maxLength -> "Debe tener exactamente ${bank.maxLength} dígitos (${value.length}/${bank.maxLength})"
            else -> null
        }
        else -> null
    }
}

// ── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    viewModel: BankAccountsViewModel? = null,
    onBack: () -> Unit = {}
) {
    var selectedBankKey    by remember { mutableStateOf("BCP") }
    var otherBankName      by remember { mutableStateOf("") }
    var accountNumber      by remember { mutableStateOf("") }
    var selectedCurrency   by remember { mutableStateOf("PEN") }
    var touched            by remember { mutableStateOf(false) }
    var selectedCountry    by remember { mutableStateOf(COUNTRY_CODES[0]) }
    var showCountryPicker  by remember { mutableStateOf(false) }

    val uiState by viewModel?.uiState?.collectAsState(initial = BankAccountsUiState())
        ?: remember { mutableStateOf(BankAccountsUiState()) }
    val context = LocalContext.current

    val bank         = BANKS.first { it.key == selectedBankKey }
    val isYapeOrPlin = selectedBankKey in listOf("Yape", "Plin")
    val fieldError   = if (touched) validate(accountNumber, bank) else null

    // Monedas disponibles según el banco seleccionado
    val availableCurrencies = bank.currencies

    // Detectar cuenta duplicada (mismo banco + mismo número)
    val resolvedBankName = if (bank.isOther) otherBankName.trim() else bank.key
    val isDuplicate = accountNumber.isNotBlank() && uiState.accounts.any { existing ->
        existing.account_number == accountNumber &&
        existing.bank_name.lowercase() == resolvedBankName.lowercase()
    }

    val canAdd = fieldError == null && accountNumber.isNotBlank() &&
        (!bank.isOther || otherBankName.trim().length >= 2) && !isDuplicate

    LaunchedEffect(selectedBankKey) {
        // Al cambiar de banco, fijar la primera moneda disponible si la actual no es compatible
        if (selectedCurrency !in bank.currencies) selectedCurrency = bank.currencies.first()
    }

    RefreshOnResume { viewModel?.loadBankAccounts() }
    LaunchedEffect(Unit) { viewModel?.loadBankAccounts() }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            android.widget.Toast.makeText(context, uiState.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel?.clearMessages()
            accountNumber = ""; otherBankName = ""; touched = false
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel?.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de Pago", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
            )
        },
        containerColor = BackgroundApp,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.05f), BackgroundApp)))
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Tarjeta: Agregar cuenta ──────────────────────────────────
            item {
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {

                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassIconBadge(
                                icon = Icons.Default.AccountBalance,
                                tint = Primary,
                                size = 44.dp,
                                iconSize = 20.dp
                            )
                            Column {
                                Text(
                                    "Agregar cuenta bancaria",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextMain
                                )
                                Text(
                                    "Selecciona el banco y el número de cuenta",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        // ── Selector de banco (grid 3×2) ─────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Banco o billetera",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecond
                            )
                            Spacer(Modifier.height(4.dp))
                            BankGrid(
                                banks = BANKS,
                                selectedKey = selectedBankKey,
                                onSelect = { key ->
                                    selectedBankKey = key
                                    accountNumber = ""
                                    otherBankName = ""
                                    touched = false
                                    selectedCountry = COUNTRY_CODES[0]
                                }
                            )
                        }

                        // ── Campo nombre banco (solo Otros) ──────────────
                        AnimatedVisibility(
                            visible = bank.isOther,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = otherBankName,
                                onValueChange = { if (it.length <= 40) otherBankName = it },
                                label = { Text("Nombre del banco o billetera", fontSize = 13.sp) },
                                placeholder = {
                                    Text(
                                        "Ej. Scotiabank, BanBif, Wise, Binance...",
                                        fontSize = 13.sp,
                                        color = TextMuted.copy(alpha = 0.6f)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = fieldColors(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Business,
                                        null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    Text(
                                        "${otherBankName.length}/40",
                                        fontSize = 10.sp,
                                        color = TextSubtle,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                }
                            )
                        }

                        // ── Campo número de cuenta ───────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isYapeOrPlin) {
                                // Fila: selector de país + campo de número
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Botón selector de código de país
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    1.dp,
                                                    if (fieldError != null) DangerColor else BorderColor,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .background(SurfaceElevated)
                                                .clickable { showCountryPicker = true }
                                                .padding(horizontal = 10.dp, vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(selectedCountry.flag, fontSize = 18.sp)
                                                Text(
                                                    selectedCountry.dialCode,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextMain
                                                )
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    null,
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showCountryPicker,
                                            onDismissRequest = { showCountryPicker = false },
                                            modifier = Modifier.background(SurfaceColor)
                                        ) {
                                            COUNTRY_CODES.forEach { country ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(country.flag, fontSize = 18.sp)
                                                            Text(
                                                                country.name,
                                                                fontSize = 13.sp,
                                                                color = TextMain
                                                            )
                                                            Text(
                                                                country.dialCode,
                                                                fontSize = 13.sp,
                                                                color = TextMuted,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedCountry = country
                                                        showCountryPicker = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Campo de número de celular
                                    OutlinedTextField(
                                        value = accountNumber,
                                        onValueChange = { input ->
                                            val filtered = input.filter { it.isDigit() }
                                            if (filtered.length <= bank.maxLength) {
                                                accountNumber = filtered
                                                touched = true
                                            }
                                        },
                                        label = { Text(bank.fieldLabel, fontSize = 13.sp) },
                                        placeholder = {
                                            Text(
                                                bank.fieldHint,
                                                fontSize = 12.sp,
                                                color = TextMuted.copy(alpha = 0.6f)
                                            )
                                        },
                                        isError = fieldError != null,
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = fieldColors(),
                                        trailingIcon = {
                                            Text(
                                                "${accountNumber.length}/${bank.maxLength}",
                                                fontSize = 10.sp,
                                                color = if (fieldError != null) DangerColor else TextSubtle,
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                        }
                                    )
                                }
                            } else {
                            OutlinedTextField(
                                value = accountNumber,
                                onValueChange = { input ->
                                    val filtered = if (bank.digitsOnly)
                                        input.filter { it.isDigit() } else input
                                    if (filtered.length <= bank.maxLength) {
                                        accountNumber = filtered
                                        touched = true
                                    }
                                },
                                label = { Text(bank.fieldLabel, fontSize = 13.sp) },
                                placeholder = {
                                    Text(
                                        bank.fieldHint,
                                        fontSize = 12.sp,
                                        color = TextMuted.copy(alpha = 0.6f)
                                    )
                                },
                                isError = fieldError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (bank.digitsOnly) KeyboardType.Number else KeyboardType.Text
                                ),
                                colors = fieldColors(),
                                trailingIcon = {
                                    Text(
                                        "${accountNumber.length}/${bank.maxLength}",
                                        fontSize = 10.sp,
                                        color = if (fieldError != null) DangerColor else TextSubtle,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                }
                            )
                            }
                            // Feedback de validación del campo
                            when {
                                fieldError != null ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, null, tint = DangerColor, modifier = Modifier.size(12.dp))
                                        Text(fieldError, fontSize = 11.sp, color = DangerColor)
                                    }
                                touched && accountNumber.isNotBlank() && !isDuplicate ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(12.dp))
                                        Text("Número válido", fontSize = 11.sp, color = SuccessColor)
                                    }
                            }
                        }

                        // ── Banner: cuenta duplicada ──────────────────────
                        AnimatedVisibility(
                            visible = isDuplicate,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DangerColor.copy(alpha = 0.08f))
                                    .border(1.dp, DangerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Info, null, tint = DangerColor, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        "Esta cuenta ya está registrada",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DangerColor
                                    )
                                    Text(
                                        "Ya tienes una cuenta con este número en $resolvedBankName.",
                                        fontSize = 11.sp,
                                        color = DangerColor.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        // ── Moneda (solo monedas del banco seleccionado) ──
                        AnimatedVisibility(
                            visible = !isYapeOrPlin,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Moneda de la cuenta",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecond
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availableCurrencies.forEach { currency ->
                                        val isSelected = currency == selectedCurrency
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(if (isSelected) Primary else SurfaceElevated)
                                                .border(1.dp, if (isSelected) Primary else BorderColor, RoundedCornerShape(50.dp))
                                                .clickable { selectedCurrency = currency }
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                currency,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else TextMuted,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Botón agregar ─────────────────────────────────
                        Button(
                            onClick = {
                                touched = true
                                if (canAdd) {
                                    val bankName = if (bank.isOther) otherBankName.trim() else bank.key
                                    val holder = uiState.currentUserName?.takeIf { it.isNotBlank() } ?: bankName
                                    // El backend valida exactamente 9 dígitos para Yape/Plin,
                                    // por eso el código de país no se envía (es solo visual).
                                    viewModel?.addBankAccount(
                                        bankName      = bankName,
                                        accountNumber = accountNumber,
                                        accountHolder = holder,
                                        currency      = selectedCurrency
                                    )
                                }
                            },
                            enabled  = canAdd && !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Agregar cuenta",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── Encabezado: Mis cuentas ──────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Mis cuentas registradas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextMain
                    )
                    if (uiState.accounts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Primary)
                                .size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${uiState.accounts.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Lista de cuentas ─────────────────────────────────────────
            when {
                uiState.isLoading && uiState.accounts.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.accounts.isEmpty() -> item {
                    GlassCard(
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(32.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassIconBadge(
                                icon = Icons.Default.AccountBalance,
                                tint = Primary,
                                size = 60.dp,
                                iconSize = 28.dp
                            )
                            Text(
                                "Sin cuentas bancarias",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextMain
                            )
                            Text(
                                "Agrega una cuenta para empezar a operar en el mercado",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> items(uiState.accounts, key = { it.id }) { account ->
                    BankAccountCard(
                        account      = account,
                        onSetDefault = { viewModel?.setDefaultAccount(account.id) },
                        onDelete     = { viewModel?.deleteBankAccount(account.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Grid de bancos (3 columnas) ───────────────────────────────────────────────

@Composable
private fun BankGrid(
    banks: List<BankDef>,
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    val rows = banks.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { bank ->
                    BankCard(
                        bank       = bank,
                        isSelected = selectedKey == bank.key,
                        modifier   = Modifier.weight(1f),
                        onClick    = { onSelect(bank.key) }
                    )
                }
                // Rellenar celdas vacías si la fila no está completa
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BankCard(
    bank: BankDef,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected)
        Brush.linearGradient(listOf(bank.color.copy(alpha = 0.12f), bank.color.copy(alpha = 0.06f)))
    else
        Brush.linearGradient(listOf(SurfaceColor, SurfaceElevated))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) bank.color else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Círculo con inicial o ícono "+"
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) bank.color
                        else bank.color.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bank.isOther) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = if (isSelected) Color.White else bank.color,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = bank.displayName.take(2).uppercase(),
                        color = if (isSelected) Color.White else bank.color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nombre del banco
            Text(
                text = bank.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) bank.color else TextMain,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Formato requerido
            Text(
                text = bank.shortHint,
                fontSize = 9.sp,
                color = if (isSelected) bank.color.copy(alpha = 0.75f) else TextSubtle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Tarjeta de cuenta guardada ────────────────────────────────────────────────

@Composable
private fun BankAccountCard(
    account: BankAccount,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    val bankColor = when (account.bank_name.lowercase()) {
        "bcp"       -> BcpColor
        "interbank" -> InterbankColor
        "bbva"      -> BbvaColor
        "yape"      -> YapeColor
        "plin"      -> PlinColor
        else        -> Primary
    }
    val initials = account.bank_name.trim().split(" ")
        .filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "??" }.take(2)

    GlassCard(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        elevation      = 2.dp,
        tint           = if (account.is_primary) bankColor else Primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar circular
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(bankColor, bankColor.copy(alpha = 0.75f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre del banco + badge "Principal"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        account.bank_name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextMain
                    )
                    if (account.is_primary) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(bankColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Principal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Número enmascarado
                Text(
                    maskAccount(account.account_number),
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                // Moneda + titular
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary.copy(alpha = 0.10f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            account.currency,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                    if (account.account_holder.isNotBlank()) {
                        Text(
                            account.account_holder,
                            fontSize = 11.sp,
                            color = TextSubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!account.is_primary) {
                    Text(
                        "Hacer principal",
                        fontSize = 11.sp,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onSetDefault() }
                            .padding(top = 2.dp)
                    )
                }
            }

            // Botón eliminar
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar cuenta",
                    tint = DangerColor.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// Enmascara el número dejando visible solo el inicio y el final
private fun maskAccount(number: String): String {
    if (number.length <= 8) return number
    val visible = minOf(4, number.length / 3)
    val masked = "•".repeat(number.length - visible * 2)
    return "${number.take(visible)} $masked ${number.takeLast(visible)}"
}

// Colores compartidos para OutlinedTextField
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Primary,
    unfocusedBorderColor = BorderColor,
    errorBorderColor     = DangerColor,
    focusedLabelColor    = Primary,
    unfocusedLabelColor  = TextMuted,
    errorLabelColor      = DangerColor,
    cursorColor          = Primary,
    focusedTextColor     = TextMain,
    unfocusedTextColor   = TextMain,
)
