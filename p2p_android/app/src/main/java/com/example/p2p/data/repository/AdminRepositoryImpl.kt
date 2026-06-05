package com.example.p2p.data.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.AdminApi
import com.example.p2p.data.remote.api.AdminDashboardResponse
import com.example.p2p.data.remote.api.AdminUser
import com.example.p2p.data.remote.api.BanUserRequest
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse
import com.example.p2p.data.remote.model.ResolveDisputeRequest
import com.example.p2p.domain.repository.AdminRepository

class AdminRepositoryImpl(
    private val api: AdminApi
) : AdminRepository {

    // ── Dashboard ─────────────────────────────────────────────────────────────

    override suspend fun getDashboardStats(): NetworkResult<AdminDashboardResponse> =
        safeCall { api.getDashboardStats() }

    // ── Disputas ──────────────────────────────────────────────────────────────

    override suspend fun getDisputes(
        page: Int,
        perPage: Int,
        status: String?
    ): NetworkResult<DisputesResponse> =
        safeCall { api.getDisputes(page, perPage, status) }

    override suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute> =
        safeCall { api.getDisputeDetail(disputeId) }

    override suspend fun takeDispute(disputeId: String): NetworkResult<Unit> =
        try {
            val response = api.takeDispute(disputeId)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(response.code(), response.message())
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }

    override suspend fun resolveDispute(
        disputeId: String,
        resolution: String,
        resolutionNote: String?
    ): NetworkResult<Unit> =
        try {
            val body = ResolveDisputeRequest(
                resolution = resolution,
                resolution_note = resolutionNote
            )
            val response = api.resolveDispute(disputeId, body)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(response.code(), response.message())
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    override suspend fun getUsers(
        page: Int,
        perPage: Int,
        role: String?
    ): NetworkResult<List<AdminUser>> =
        try {
            val response = api.getUsers(page, perPage, role)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.users)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }

    override suspend fun banUser(userId: String, banned: Boolean): NetworkResult<Unit> =
        try {
            val response = api.banUser(userId, BanUserRequest(banned))
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(response.code(), response.message())
        } catch (e: Exception) {
            NetworkResult.Error(-1, e.message ?: "Error de conexión")
        }

    // ── Reclamos ──────────────────────────────────────────────────────────────

    override suspend fun getComplaints(
        page: Int,
        perPage: Int,
        status: String?
    ): NetworkResult<com.example.p2p.data.remote.model.ComplaintsResponse> =
        safeCall { api.getComplaints(page, perPage, status) }

    override suspend fun getComplaintDetail(
        complaintId: String
    ): NetworkResult<com.example.p2p.data.remote.model.Complaint> =
        safeCall { api.getComplaintDetail(complaintId) }

    override suspend fun resolveComplaint(
        complaintId: String,
        adminNote: String
    ): NetworkResult<com.example.p2p.data.remote.model.Complaint> =
        safeCall { api.resolveComplaint(complaintId, com.example.p2p.data.remote.api.ResolveComplaintRequest(adminNote)) }


    // ── Helper ────────────────────────────────────────────────────────────────

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
