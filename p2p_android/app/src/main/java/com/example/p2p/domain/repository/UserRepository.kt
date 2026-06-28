package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.User

interface UserRepository {
    suspend fun getMe(): NetworkResult<User>
    suspend fun updateProfile(fullName: String, phone: String?): NetworkResult<User>
    suspend fun submitKyc(
        dniFront: ByteArray,
        dniBack: ByteArray,
        selfie: ByteArray,
        signature: ByteArray? = null
    ): NetworkResult<Unit>
    suspend fun uploadSignature(signature: ByteArray): NetworkResult<Unit>
}
