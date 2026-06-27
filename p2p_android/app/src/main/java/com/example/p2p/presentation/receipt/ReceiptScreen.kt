package com.example.p2p.presentation.receipt

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.p2p.presentation.common.RefreshOnResume
import com.example.p2p.presentation.transaction.TransactionUiState
import com.example.p2p.presentation.transaction.TransactionViewModel
import com.example.p2p.ui.theme.*

@Composable
fun ReceiptScreen(
    transactionId: String? = null,
    viewModel: TransactionViewModel? = null,
    onNavigateToRating: (String) -> Unit = {},
    onNavigateToMarket: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel?.uiState?.collectAsState(initial = TransactionUiState()) ?: remember { mutableStateOf(TransactionUiState()) }

    RefreshOnResume {
        if (transactionId != null) {
            viewModel?.loadTransaction(transactionId, showLoading = false)
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel?.loadTransaction(transactionId)
        }
    }

    val txn = uiState.transaction
    val buyerName  = txn?.buyer_name  ?: "Comprador"
    val vendorName = txn?.vendor_name ?: "Vendedor"
    val rate       = txn?.exchange_rate ?: 3.780
    val amountFiat = txn?.amount_to ?: 0.0
    val receiptPdfUrl = txn?.receipt_pdf_url

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SuccessColor, Color(0xFF059669))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Éxito",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Intercambio Exitoso!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Los fondos ya están en tu billetera.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceColor)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp)
            ) {
                Text(
                    text = "COMPROBANTE P2P",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transactionId?.take(8) ?: "#TX-9982",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReceiptRow(label = "Comprador:", value = buyerName, valueColor = TextMain)
                ReceiptRow(label = "Vendedor:", value = vendorName, valueColor = TextMain)
                ReceiptRow(label = "Tasa Aplicada:", value = "S/ $rate", valueColor = TextMain)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auditoría OCR:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "#994812",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SuccessColor
                        )
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SuccessColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Auténtico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SuccessColor
                        )
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monto Acreditado:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMain
                    )
                    Text(
                        text = "S/ $amountFiat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (receiptPdfUrl.isNullOrBlank()) {
                    Toast.makeText(context, "El PDF aun no esta disponible.", Toast.LENGTH_SHORT).show()
                } else {
                    val request = DownloadManager.Request(Uri.parse(receiptPdfUrl))
                        .setTitle("voucher-${transactionId?.take(8) ?: "peruexchange"}.pdf")
                        .setDescription("Voucher de compra PeruExchange")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            "voucher-${transactionId?.take(8) ?: "peruexchange"}.pdf"
                        )
                    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                    Toast.makeText(context, "Descargando PDF...", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Descargar Comprobante",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onNavigateToMarket,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volver al Mercado",
                fontSize = 14.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = TextMain,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
