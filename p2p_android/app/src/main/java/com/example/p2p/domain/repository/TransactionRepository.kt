package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.CreateTransactionRequest
import com.example.p2p.data.remote.model.Transaction

interface TransactionRepository {
    suspend fun listTransactions(status: String? = null): NetworkResult<List<Transaction>>
    suspend fun getPendingTransactions(): NetworkResult<List<Transaction>>
    suspend fun getTransaction(id: String): NetworkResult<Transaction>
    suspend fun createTransaction(request: CreateTransactionRequest): NetworkResult<Transaction>
    suspend fun uploadVoucher(id: String, imageUrl: String): NetworkResult<Unit>
    suspend fun uploadVoucherWithBase64(id: String, base64: String): NetworkResult<Unit>
    suspend fun updateStatus(id: String, newStatus: String): NetworkResult<Transaction>
    suspend fun confirmTransaction(id: String): NetworkResult<Unit>
}
