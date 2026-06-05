package com.example.p2p.data.remote.api

import com.example.p2p.data.remote.model.Dispute
import com.example.p2p.data.remote.model.DisputesResponse
import com.example.p2p.data.remote.model.ResolveDisputeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

// ── Response models ───────────────────────────────────────────────────────────


data class ResolveComplaintRequest
    (val admin_note: String

            )
data class AdminUsersStats(
    val total: Int,
    val active: Int
)

data class AdminTransactionsStats(
    val total: Int,
    val completed: Int
)

data class AdminDisputesStats(
    val pending: Int,
    val resolved: Int
)

/**
 * Mapea GET /api/v1/admin/dashboard
 */
data class AdminDashboardResponse(
    val users: AdminUsersStats,
    val transactions: AdminTransactionsStats,
    val disputes: AdminDisputesStats,
    val total_volume: Double
) {
    // Helpers para que AdminScreen siga funcionando igual
    val total_users: Int get() = users.total
    val total_transactions: Int get() = transactions.total
    val pending_disputes: Int get() = disputes.pending
}

data class AdminUser(
    val id: String,
    val email: String,
    val full_name: String,
    val phone: String?,
    val role: String,
    val kyc_verified: Boolean,
    val rating: Double,
    val total_transactions: Int,
    val is_active: Boolean,
    val is_banned: Boolean,
    val created_at: String
)

data class AdminUsersResponse(
    val users: List<AdminUser>,
    val pagination: com.example.p2p.data.remote.model.PaginationMeta?
)

data class BanUserRequest(val banned: Boolean)

// ── API Interface ─────────────────────────────────────────────────────────────

interface AdminApi {

    /** GET /api/v1/admin/dashboard */
    @GET("admin/dashboard")
    suspend fun getDashboardStats(): Response<AdminDashboardResponse>

    // ── Usuarios ──────────────────────────────────────────────────────────────

    /** GET /api/v1/admin/users */
    @GET("admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("role") role: String? = null,
        @Query("active") active: String? = null
    ): Response<AdminUsersResponse>

    /** PATCH /api/v1/admin/users/{id}/ban */
    @PATCH("admin/users/{id}/ban")
    suspend fun banUser(
        @Path("id") userId: String,
        @Body body: BanUserRequest
    ): Response<Map<String, Any>>

    // ── Disputas ──────────────────────────────────────────────────────────────

    /** GET /api/v1/admin/disputes */
    @GET("admin/disputes")
    suspend fun getDisputes(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String? = null
    ): Response<DisputesResponse>

    /** GET /api/v1/admin/disputes/{id} */
    @GET("admin/disputes/{id}")
    suspend fun getDisputeDetail(
        @Path("id") disputeId: String
    ): Response<Dispute>

    /** PATCH /api/v1/admin/disputes/{id}/take */
    @PATCH("admin/disputes/{id}/take")
    suspend fun takeDispute(
        @Path("id") disputeId: String
    ): Response<Map<String, String>>

    /** PATCH /api/v1/admin/disputes/{id}/resolve */
    @PATCH("admin/disputes/{id}/resolve")
    suspend fun resolveDispute(
        @Path("id") disputeId: String,
        @Body body: ResolveDisputeRequest
    ): Response<Map<String, String>>

    // ── Reclamos ──────────────────────────────────────────────────────────────

    /** GET /api/v1/admin/complaints */
    @GET("admin/complaints")
    suspend fun getComplaints(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String? = null
    ): Response<com.example.p2p.data.remote.model.ComplaintsResponse>

    /** GET /api/v1/admin/complaints/{id} */
    @GET("admin/complaints/{id}")
    suspend fun getComplaintDetail(
        @Path("id") complaintId: String
    ): Response<com.example.p2p.data.remote.model.Complaint>

    /** PATCH /api/v1/admin/complaints/{id}/resolve */
    @PATCH("admin/complaints/{id}/resolve")
    suspend fun resolveComplaint(
        @Path("id") complaintId: String,
        @Body body: ResolveComplaintRequest
    ): Response<com.example.p2p.data.remote.model.Complaint>
}


