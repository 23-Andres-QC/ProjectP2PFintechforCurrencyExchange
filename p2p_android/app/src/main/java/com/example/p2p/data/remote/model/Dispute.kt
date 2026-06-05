package com.example.p2p.data.remote.model

// ── Modelos de respuesta del backend ─────────────────────────────────────────

data class DisputeTransaction(
    val id: String,
    val amount_from: Double,
    val amount_to: Double,
    val exchange_rate: Double,
    val status: String,
    val buyer_id: String,
    val vendor_id: String
)

/**
 * Mapea exactamente la respuesta de:
 *   GET  /api/v1/disputes/my-disputes
 *   GET  /api/v1/disputes/{id}
 *   GET  /api/v1/admin/disputes
 *   GET  /api/v1/admin/disputes/{id}
 */
data class Dispute(
    val id: String,
    val transaction_id: String,
    val initiator_id: String,
    val initiator_name: String?,
    val reason: String,
    val description: String?,
    val status: String,                // open | under_review | resolved | closed
    val resolved_by: String?,
    val resolution: String?,           // favour_buyer | favour_vendor
    val resolution_note: String?,
    val resolved_at: String?,
    val created_at: String,
    val updated_at: String,
    val transaction: DisputeTransaction? = null
)

data class DisputesResponse(
    val disputes: List<Dispute>,
    val pagination: PaginationMeta? = null
)

data class PaginationMeta(
    val page: Int,
    val per_page: Int,
    val total: Int,
    val pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)

// ── Requests ──────────────────────────────────────────────────────────────────

/**
 * Body para POST /api/v1/transactions/{id}/dispute
 */
data class CreateDisputeRequest(
    val reason: String,
    val description: String?
)

/**
 * Body para PATCH /api/v1/admin/disputes/{id}/resolve
 */
data class ResolveDisputeRequest(
    val resolution: String,           // "favour_buyer" | "favour_vendor"
    val resolution_note: String?
)

// ── Constantes de razón (deben coincidir con Dispute.VALID_REASONS en Python) ─

object DisputeReason {
    const val PAYMENT_NOT_RECEIVED = "payment_not_received"
    const val WRONG_AMOUNT         = "wrong_amount"
    const val VOUCHER_FAKE         = "voucher_fake"
    const val NO_RESPONSE          = "no_response"
    const val OTHER                = "other"

    /** Texto legible en español para mostrar en la UI */
    fun label(reason: String) = when (reason) {
        PAYMENT_NOT_RECEIVED -> "Pago no recibido"
        WRONG_AMOUNT         -> "Monto incorrecto"
        VOUCHER_FAKE         -> "Comprobante falso"
        NO_RESPONSE          -> "Sin respuesta"
        OTHER                -> "Otro motivo"
        else                 -> reason
    }

    val all = listOf(
        PAYMENT_NOT_RECEIVED,
        WRONG_AMOUNT,
        VOUCHER_FAKE,
        NO_RESPONSE,
        OTHER
    )
}
