package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateBankAccountRequest

interface BankAccountRepository {
    suspend fun listAccounts(): NetworkResult<List<BankAccount>>
    suspend fun createAccount(request: CreateBankAccountRequest): NetworkResult<BankAccount>
    suspend fun deleteAccount(id: String): NetworkResult<Unit>
    suspend fun setDefault(id: String): NetworkResult<Unit>
}
