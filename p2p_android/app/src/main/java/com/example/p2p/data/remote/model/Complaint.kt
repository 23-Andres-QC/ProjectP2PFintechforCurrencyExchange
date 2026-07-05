package com.example.p2p.data.remote.model

import com.google.gson.annotations.SerializedName

enum class ComplaintType(val apiValue: String, val label: String) {
    PLATFORM_ERROR("platform_error", "Error en plataforma"),
    TRANSACTION_ISSUE("transaction_issue", "Problema con transaccion"),
    ACCOUNT_ISSUE("account_issue", "Problema con mi cuenta"),
    PAYMENT_ISSUE("payment_issue", "Problema con pago"),
    OTHER("other", "Otro");

    companion object {
        fun label(type: String): String =
            entries.firstOrNull { it.apiValue == type || it.name == type }?.label ?: type
    }
}

enum class ComplaintStatus(val apiValue: String, val label: String) {
    PENDING("pending", "Pendiente"),
    UNDER_REVIEW("under_review", "En revision"),
    RESOLVED("resolved", "Resuelto"),
    CLOSED("closed", "Cerrado");

    companion object {
        fun label(status: String): String =
            entries.firstOrNull { it.apiValue == status || it.name == status }?.label ?: status
    }
}

data class Complaint(
    @SerializedName("id")          val id: String,
    @SerializedName("type")        val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("status")      val status: String,
    @SerializedName("created_at")  val created_at: String,
    @SerializedName("updated_at")  val updated_at: String? = null,
    @SerializedName("admin_note")  val admin_note: String? = null
)

data class ComplaintsResponse(
    @SerializedName("complaints")  val complaints: List<Complaint>,
    @SerializedName("pagination")  val pagination: ComplaintPagination? = null
)

data class ComplaintPagination(
    @SerializedName("page")    val page: Int,
    @SerializedName("pages")   val pages: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total")   val total: Int
)

data class CreateComplaintRequest(
    @SerializedName("type")        val type: String,
    @SerializedName("description") val description: String
)
