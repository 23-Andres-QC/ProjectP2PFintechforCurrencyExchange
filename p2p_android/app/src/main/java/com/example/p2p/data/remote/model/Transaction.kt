package com.example.p2p.data.remote.model

data class Transaction(
    val id: String,
    val offer_id: String,
    val buyer_id: String,
    val vendor_id: String,
    val buyer_name: String? = null,
    val vendor_name: String? = null,
    val amount_from: Double,
    val amount_to: Double,
    val exchange_rate: Double,
    val status: String,
    val buyer_payment_account: String?,
    val vendor_payment_account: String?,
    val created_at: String,
    val updated_at: String?,
    val vendor_voucher_url: String? = null  // ← AGREGAR
)


data class TransactionsResponse(
    val transactions: List<Transaction>
)

data class CreateTransactionRequest(
    val offer_id: String,
    val amount_from: Double,
    val amount_to: Double,
    val buyer_payment_account: String,
    val vendor_payment_account: String
)
