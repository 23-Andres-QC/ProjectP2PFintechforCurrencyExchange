package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.AdminDashboardResponse
import com.example.p2p.data.remote.api.AdminUser
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse

interface AdminRepository {

    // ── Dashboard ─────────────────────────────────────────────────────────────
    suspend fun getDashboardStats(): NetworkResult<AdminDashboardResponse>

    // ── Disputas ──────────────────────────────────────────────────────────────
    suspend fun getDisputes(
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null
    ): NetworkResult<DisputesResponse>

    suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute>

    /** Tomar una disputa para revisión (under_review). */
    suspend fun takeDispute(disputeId: String): NetworkResult<Unit>

    /** Resolver disputa. resolution = "favour_buyer" | "favour_vendor" */
    suspend fun resolveDispute(
        disputeId: String,
        resolution: String,
        resolutionNote: String? = null
    ): NetworkResult<Unit>

    // ── Usuarios ──────────────────────────────────────────────────────────────
    suspend fun getUsers(
        page: Int = 1,
        perPage: Int = 20,
        role: String? = null
    ): NetworkResult<List<AdminUser>>

    suspend fun banUser(userId: String, banned: Boolean): NetworkResult<Unit>

    // ── Reclamos ──────────────────────────────────────────────────────────────
    suspend fun getComplaints(
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null
    ): NetworkResult<com.example.p2p.data.remote.model.ComplaintsResponse>

    suspend fun getComplaintDetail(
        complaintId: String
    ): NetworkResult<com.example.p2p.data.remote.model.Complaint>

    suspend fun resolveComplaint(
        complaintId: String,
        adminNote: String
    ): NetworkResult<com.example.p2p.data.remote.model.Complaint>


}
