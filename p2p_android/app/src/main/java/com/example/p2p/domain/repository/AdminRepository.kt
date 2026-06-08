package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.api.AdminDashboardResponse
import com.example.p2p.data.remote.api.AdminUser
import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse

interface AdminRepository {

    suspend fun getDashboardStats(): NetworkResult<AdminDashboardResponse>

    suspend fun getDisputes(
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null
    ): NetworkResult<DisputesResponse>

    suspend fun getDisputeDetail(disputeId: String): NetworkResult<Dispute>

    suspend fun takeDispute(disputeId: String): NetworkResult<Unit>

    suspend fun resolveDispute(
        disputeId: String,
        resolution: String,
        resolutionNote: String? = null
    ): NetworkResult<Unit>

    suspend fun getUsers(
        page: Int = 1,
        perPage: Int = 20,
        role: String? = null
    ): NetworkResult<List<AdminUser>>

    suspend fun banUser(userId: String, banned: Boolean): NetworkResult<Unit>

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
