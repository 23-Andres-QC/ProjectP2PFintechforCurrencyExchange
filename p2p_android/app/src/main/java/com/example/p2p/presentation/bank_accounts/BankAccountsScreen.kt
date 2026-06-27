package com.example.p2p.presentation.bank_accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BbvaColor
import com.example.p2p.ui.theme.BcpColor
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.InterbankColor
import com.example.p2p.ui.theme.PlinColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SuccessColor
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.YapeColor

private data class BankChip(val name: String, val color: Color)

private val bankChips = listOf(
    BankChip("BCP",       BcpColor),
    BankChip("Interbank", InterbankColor),
    BankChip("BBVA",      BbvaColor),
    BankChip("Yape",      YapeColor),
    BankChip("Plin",      PlinColor),
    BankChip("Wise",      Color(0xFF00B9FF)),
    BankChip("Binance",   Color(0xFFF0B90B)),
    BankChip("Otro",      Color(0xFF6B7280)),
)

private val currencyOptions = listOf(
    "PEN", "USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP"
)

private val mobileWalletBanks = listOf("yape", "plin")
private val strictDigitBanks  = listOf("bcp", "interbank", "bbva")

private fun cleanAccountNumber(number: String) = number.replace("-", "").replace(" ", "")

private fun validateAccountNumber(number: String, bank: String): String? {
    val bankLower = bank.lowercase()
    return when {
        number.isBlank() -> "El número de cuenta es obligatorio"
        bankLower in mobileWalletBanks -> {
            val d = cleanAccountNumber(number)
            when {
                !d.all { it.isDigit() } -> "Solo dígitos"
                d.length != 9           -> "Yape/Plin requiere exactamente 9 dígitos"
                else                    -> null
            }
        }
        bankLower in strictDigitBanks -> {
            val d = cleanAccountNumber(number)
            when {
                !d.all { it.isDigit() } -> "Solo dígitos (sin letras)"
                d.length != 20          -> "El CCI debe tener exactamente 20 dígitos"
                else                    -> null
            }
        }
        else -> {
            val clean = number.trim()
            when {
                clean.length < 4  -> "Mínimo 4 caracteres"
                clean.length > 60 -> "Máximo 60 caracteres"
                else              -> null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    viewModel: BankAccountsViewModel? = null,
    onBack: () -> Unit = {}
) {
    var selectedBank     by remember { mutableStateOf("BCP") }
    var accountNumber    by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("PEN") }
    var accountTouched   by remember { mutableStateOf(false) }

    val uiState by viewModel?.uiState?.collectAsState(initial = BankAccountsUiState())
        ?: remember { mutableStateOf(BankAccountsUiState()) }
    val context = LocalContext.current

    val isYapeOrPlin   = selectedBank.lowercase() in mobileWalletBanks
    val isInternational = selectedBank.lowercase() !in (mobileWalletBanks + strictDigitBanks)
    val accountError   = if (accountTouched) validateAccountNumber(accountNumber, selectedBank) else null
    val canAdd         = validateAccountNumber(accountNumber, selectedBank) == null && accountNumber.isNotBlank()

    RefreshOnResume {
        viewModel?.loadBankAccounts()
    }

    LaunchedEffect(Unit) { viewModel?.loadBankAccounts() }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            android.widget.Toast.makeText(context, uiState.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel?.clearMessages()
            accountNumber  = ""
            accountTouched = false
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
                title = { Text("Mis Cuentas Bancarias", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
            )
        },
        containerColor = BackgroundApp,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            item {
                Text("Agregar Cuenta Bancaria", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecciona tu banco", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        bankChips.forEach { chip ->
                            val isSelected = chip.name == selectedBank
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (isSelected) chip.color else SurfaceColor)
                                    .border(1.dp, if (isSelected) chip.color else BorderColor, RoundedCornerShape(50.dp))
                                    .clickable { selectedBank = chip.name; accountNumber = ""; accountTouched = false }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    chip.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = {
                            val maxLen = when {
                                isYapeOrPlin    -> 9
                                isInternational -> 60
                                else            -> 20
                            }
                            val allowed = when {
                                isYapeOrPlin                  -> it.all { c -> c.isDigit() }
                                isInternational               -> true
                                else                          -> it.all { c -> c.isDigit() }
                            }
                            if (it.length <= maxLen && allowed) {
                                accountNumber  = it
                                accountTouched = true
                            }
                        },
                        label = {
                            Text(
                                when {
                                    isYapeOrPlin    -> "Número de celular"
                                    isInternational -> "Número / IBAN / Wallet"
                                    else            -> "Número de CCI (20 dígitos)"
                                },
                                fontSize = 13.sp
                            )
                        },
                        placeholder = {
                            Text(
                                when {
                                    isYapeOrPlin    -> "9 dígitos (ej. 987654321)"
                                    isInternational -> "Ej. IBAN, dirección wallet, etc."
                                    else            -> "20 dígitos CCI (ej. 00219100987654321234)"
                                },
                                fontSize = 13.sp,
                                color = TextMuted.copy(alpha = 0.6f),
                            )
                        },
                        isError = accountError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isInternational) KeyboardType.Text else KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor  = Primary,
                            unfocusedBorderColor = BorderColor,
                            errorBorderColor    = DangerColor,
                            focusedLabelColor   = Primary,
                            unfocusedLabelColor = TextMuted,
                            errorLabelColor     = DangerColor,
                            cursorColor         = Primary,
                        ),
                    )
                    if (accountError != null) {
                        Text(accountError, fontSize = 11.sp, color = DangerColor, modifier = Modifier.padding(start = 4.dp))
                    } else if (accountTouched && accountNumber.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(12.dp))
                            Text("Número válido", fontSize = 11.sp, color = SuccessColor)
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Moneda de la cuenta", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyOptions.forEach { currency ->
                            val isSelected = currency == selectedCurrency
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (isSelected) Primary else SurfaceColor)
                                    .border(1.dp, if (isSelected) Primary else BorderColor, RoundedCornerShape(50.dp))
                                    .clickable { selectedCurrency = currency }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    currency,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        accountTouched = true
                        if (canAdd) {
                            val holder = uiState.currentUserName?.takeIf { it.isNotBlank() } ?: selectedBank
                            viewModel?.addBankAccount(
                                bankName      = selectedBank,
                                accountNumber = accountNumber,
                                accountHolder = holder,
                                currency      = selectedCurrency
                            )
                        }
                    },
                    enabled  = canAdd,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar Cuenta", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            item {
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mis cuentas registradas", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                    if (uiState.accounts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("${uiState.accounts.size}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.accounts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else if (uiState.accounts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceColor)
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                .background(Primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = Primary, modifier = Modifier.size(26.dp))
                        }
                        Text("No tienes cuentas bancarias registradas.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(uiState.accounts) { account ->
                    BankAccountCard(account = account, onDelete = { viewModel?.deleteBankAccount(account.id) })
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun BankAccountCard(account: BankAccount, onDelete: () -> Unit) {
    val bankColor = when (account.bank_name.lowercase()) {
        "bcp"       -> BcpColor
        "interbank" -> InterbankColor
        "bbva"      -> BbvaColor
        "yape"      -> YapeColor
        "plin"      -> PlinColor
        "wise"      -> Color(0xFF00B9FF)
        "binance"   -> Color(0xFFF0B90B)
        else        -> Primary
    }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
        border    = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(bankColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.bank_name.firstOrNull()?.toString() ?: "B",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(account.bank_name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(account.currency, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
                Text(account.account_number, fontSize = 13.sp, color = TextMuted)
                if (account.account_holder.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Titular: ${account.account_holder}", fontSize = 11.sp, color = TextMuted.copy(alpha = 0.7f))
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = DangerColor)
            }
        }
    }
}
