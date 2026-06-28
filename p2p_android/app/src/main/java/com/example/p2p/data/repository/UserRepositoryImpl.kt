package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.UserApi
import com.example.p2p.data.remote.model.User
import com.example.p2p.domain.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepositoryImpl(
    private val api: UserApi
) : UserRepository {

    override suspend fun getMe(): NetworkResult<User> {
        return try {
            val response = api.getMe()
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

    override suspend fun updateProfile(fullName: String, phone: String?): NetworkResult<User> {
        return try {
            val fields = buildMap<String, String?> {
                put("full_name", fullName)
                if (phone != null) put("phone", phone)
            }
            val response = api.updateProfile(fields)
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

    override suspend fun submitKyc(
        dniFront: ByteArray,
        dniBack: ByteArray,
        selfie: ByteArray,
        signature: ByteArray?
    ): NetworkResult<Unit> {
        return try {
            val dniFrontPart = MultipartBody.Part.createFormData(
                "dni_front",
                "dni_front.jpg",
                dniFront.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val dniBackPart = MultipartBody.Part.createFormData(
                "dni_back",
                "dni_back.jpg",
                dniBack.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val selfiePart = MultipartBody.Part.createFormData(
                "selfie",
                "selfie.jpg",
                selfie.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val signaturePart = signature?.let {
                MultipartBody.Part.createFormData(
                    "signature",
                    "signature.jpg",
                    it.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            val response = api.submitKyc(dniFrontPart, dniBackPart, selfiePart, signaturePart)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "An error occurred")
        }
    }

    override suspend fun uploadSignature(signature: ByteArray): NetworkResult<Unit> {
        return try {
            val signaturePart = MultipartBody.Part.createFormData(
                "signature",
                "signature.jpg",
                signature.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val response = api.uploadSignature(signaturePart)
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
