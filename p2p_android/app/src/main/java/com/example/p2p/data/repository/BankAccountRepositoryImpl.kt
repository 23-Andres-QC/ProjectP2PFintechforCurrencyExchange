package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.BankAccountsApi
import com.example.p2p.data.remote.model.BankAccount
import com.example.p2p.data.remote.model.CreateBankAccountRequest
import com.example.p2p.domain.repository.BankAccountRepository

class BankAccountRepositoryImpl(
    private val api: BankAccountsApi
) : BankAccountRepository {

    override suspend fun listAccounts(): NetworkResult<List<BankAccount>> {
        return try {
            val response = api.listAccounts()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body.bank_accounts)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun createAccount(request: CreateBankAccountRequest): NetworkResult<BankAccount> {
        return try {
            val response = api.createAccount(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun deleteAccount(id: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteAccount(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun setDefault(id: String): NetworkResult<Unit> {
        return try {
            val response = api.setDefault(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }
}
