package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.ComplaintApi
import com.example.p2p.data.remote.model.Complaint
import com.example.p2p.data.remote.model.ComplaintsResponse
import com.example.p2p.data.remote.model.CreateComplaintRequest
import com.example.p2p.domain.repository.ComplaintRepository

class ComplaintsRepositoryImpl(
    private val api: ComplaintApi
) : ComplaintRepository {

    override suspend fun getMyComplaints(page: Int, perPage: Int): NetworkResult<ComplaintsResponse> =
        safeCall { api.getMyComplaints(page, perPage) }

    override suspend fun createComplaint(request: CreateComplaintRequest): NetworkResult<Complaint> =
        safeCall { api.createComplaint(request) }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): NetworkResult<T> =
        try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }
}