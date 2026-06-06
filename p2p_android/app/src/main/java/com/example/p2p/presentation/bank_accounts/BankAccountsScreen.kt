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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.ui.theme.BackgroundApp
import com.example.p2p.ui.theme.BbvaColor
import com.example.p2p.ui.theme.BcpColor
import com.example.p2p.ui.theme.BorderColor
import com.example.p2p.ui.theme.DangerColor
import com.example.p2p.ui.theme.InterbankColor
import com.example.p2p.ui.theme.PlinColor
import com.example.p2p.ui.theme.Primary
import com.example.p2p.ui.theme.SurfaceColor
import com.example.p2p.ui.theme.TextMain
import com.example.p2p.ui.theme.TextMuted
import com.example.p2p.ui.theme.YapeColor

private data class BankChip(val name: String, val color: Color)

private val bankChips = listOf(
    BankChip("BCP", BcpColor),
    BankChip("Interbank", InterbankColor),
    BankChip("BBVA", BbvaColor),
    BankChip("Yape", YapeColor),
    BankChip("Plin", PlinColor),
)

private val currencyOptions = listOf("PEN", "USD", "EUR")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    viewModel: BankAccountsViewModel? = null,
    onBack: () -> Unit = {}
) {
    var selectedBank by remember { mutableStateOf("BCP") }
    var accountNumber by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("PEN") }
    val uiState by viewModel?.uiState?.collectAsState(initial = BankAccountsUiState()) ?: remember { mutableStateOf(BankAccountsUiState()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var holderName by remember(uiState.currentUserName) {
        mutableStateOf(uiState.currentUserName ?: "")
    }

    val isYapeOrPlin = selectedBank == "Yape" || selectedBank == "Plin"
    val accountError = when {
        accountNumber.isBlank() -> null
        isYapeOrPlin && accountNumber.length != 9 -> "El celular debe tener 9 dígitos"
        isYapeOrPlin && !accountNumber.all { it.isDigit() } -> "Solo números"
        !isYapeOrPlin && accountNumber.length != 20 -> "El CCI debe tener 20 dígitos"
        !isYapeOrPlin && !accountNumber.all { it.isDigit() } -> "Solo números"
        else -> null
    }
    val holderError = when {
        holderName.isBlank() -> null
        holderName.length < 3 -> "Nombre muy corto"
        else -> null
    }
    val canAdd = holderName.isNotBlank() && accountNumber.isNotBlank() && accountError == null && holderError == null

    LaunchedEffect(Unit) {
        viewModel?.loadBankAccounts()
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            android.widget.Toast.makeText(context, uiState.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel?.clearMessages()
            accountNumber = ""
            holderName = uiState.currentUserName ?: ""
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
                title = {
                    Text(
                        text = "Mis Cuentas Bancarias",
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextMain,
                        )
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
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Existing accounts
            if (uiState.isLoading && uiState.accounts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = Primary)
                    }
                }
            } else if (uiState.accounts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No tienes cuentas bancarias registradas.", color = TextMuted)
                    }
                }
            } else {
                items(uiState.accounts) { account ->
                    BankAccountCard(
                        account = account,
                        onDelete = { viewModel?.deleteBankAccount(account.id) }
                    )
                }
            }

            // Divider + add section
            item {
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Agregar Cuenta Bancaria",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextMain,
                )
                Spacer(Modifier.height(16.dp))
            }

            // Bank selector chips
            item {
                Text(
                    text = "Selecciona tu banco",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    bankChips.forEach { chip ->
                        val isSelected = chip.name == selectedBank
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) Primary else SurfaceColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else BorderColor,
                                    shape = RoundedCornerShape(50.dp),
                                )
                                .clickable { selectedBank = chip.name }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = chip.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                            )
                        }
                    }
                }
            }

            // Holder name field — oculto para Yape/Plin
            if (!isYapeOrPlin) {
                item {
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Nombre del titular", fontSize = 13.sp) },
                        placeholder = { Text("Ej. Juan Pérez", fontSize = 13.sp, color = TextMuted.copy(alpha = 0.6f)) },
                        isError = holderError != null,
                        supportingText = if (holderError != null) {
                            { Text(holderError, color = DangerColor, fontSize = 11.sp) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            errorBorderColor = DangerColor,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextMuted,
                            cursorColor = Primary,
                        ),
                    )
                }
            }

            // Account number field
            item {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { 
                        val maxLength = if (isYapeOrPlin) 9 else 20
                        if (it.length <= maxLength && it.all { c -> c.isDigit() }) {
                            accountNumber = it
                        }
                    },
                    label = { Text(if (isYapeOrPlin) "Número de celular" else "Número de CCI", fontSize = 13.sp) },
                    placeholder = {
                        Text(
                            if (isYapeOrPlin) "Ej. 987654321" else "Ej. 00219100987654321200",
                            fontSize = 13.sp,
                            color = TextMuted.copy(alpha = 0.6f),
                        )
                    },
                    isError = accountError != null,
                    supportingText = when {
                        accountError != null -> { { Text(accountError, color = DangerColor, fontSize = 11.sp) } }
                        accountNumber.isNotBlank() -> { { Text("✓ Válido", color = Color(0xFF2E7D32), fontSize = 11.sp) } }
                        else -> { { Text(if (isYapeOrPlin) "9 dígitos" else "20 dígitos", fontSize = 11.sp, color = TextMuted) } }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        errorBorderColor = DangerColor,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = Primary,
                    ),
                )
            }

            // Currency selector
            item {
                Text(
                    text = "Moneda de la cuenta",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencyOptions.forEach { currency ->
                        val isSelected = currency == selectedCurrency
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) Primary else SurfaceColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else BorderColor,
                                    shape = RoundedCornerShape(50.dp),
                                )
                                .clickable { selectedCurrency = currency }
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = currency,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                            )
                        }
                    }
                }
            }

            // Add button
            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (canAdd) {
                            viewModel?.addBankAccount(
                                bankName = selectedBank,
                                accountNumber = accountNumber,
                                accountHolder = if (isYapeOrPlin) selectedBank else holderName.trim(),
                                currency = selectedCurrency
                            )
                        }
                    },
                    enabled = canAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Agregar Cuenta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun BankAccountCard(account: BankAccount, onDelete: () -> Unit) {
    val color = when (account.bank_name) {
        "BCP" -> BcpColor
        "Interbank" -> InterbankColor
        "BBVA" -> BbvaColor
        "Yape" -> YapeColor
        "Plin" -> PlinColor
        else -> Primary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color),
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
                Text(
                    text = account.bank_name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextMain,
                )
                Text(
                    text = account.account_number,
                    fontSize = 13.sp,
                    color = TextMuted,
                )
                if (!account.account_holder.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Titular: ${account.account_holder}",
                        fontSize = 11.sp,
                        color = TextMuted.copy(alpha = 0.7f),
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = DangerColor,
                )
            }
        }
    }
}