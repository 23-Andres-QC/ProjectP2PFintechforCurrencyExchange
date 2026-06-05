package com.example.p2p.data.remote.model

import com.google.gson.annotations.SerializedName

enum class ComplaintType(val label: String) {
    PLATFORM_ERROR("Error en plataforma"),
    TRANSACTION_ISSUE("Problema con transacción"),
    ACCOUNT_ISSUE("Problema con mi cuenta"),
    PAYMENT_ISSUE("Problema con pago"),
    OTHER("Otro");

    companion object {
        fun label(type: String): String =
            entries.firstOrNull { it.name == type }?.label ?: type
    }
}

enum class ComplaintStatus(val label: String) {
    PENDING("Pendiente"),
    IN_REVIEW("En revisión"),
    RESOLVED("Resuelto"),
    CLOSED("Cerrado")
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