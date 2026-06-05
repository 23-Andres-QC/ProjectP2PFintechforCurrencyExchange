"""Disputes routes — /api/v1/disputes/*

Endpoints para usuarios finales (comprador y vendedor).
Los endpoints de administración están en /api/v1/admin/.
"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from app.services.dispute_service import DisputeService

disputes_bp = Blueprint('disputes', __name__, url_prefix='/disputes')


# ── Helpers ──────────────────────────────────────────────────────────────────

def _paginate_params():
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))
    return page, per_page


# ── GET /disputes/my-disputes ─────────────────────────────────────────────────
@disputes_bp.route('/my-disputes', methods=['GET'])
@jwt_required()
def my_disputes():
    """
    Devuelve todas las disputas donde el usuario autenticado
    es comprador o vendedor de la transacción.

    Query params:
        page     (int, default 1)
        per_page (int, default 20, max 50)
    """
    user_id = get_jwt_identity()
    page, per_page = _paginate_params()

    pagination = DisputeService.get_my_disputes(user_id, page, per_page)

    return {
        'disputes': [d.to_dict(include_transaction=True) for d in pagination.items],
        'pagination': {
            'page':       pagination.page,
            'per_page':   pagination.per_page,
            'total':      pagination.total,
            'pages':      pagination.pages,
            'has_next':   pagination.has_next,
            'has_prev':   pagination.has_prev,
        }
    }, 200


# ── GET /disputes/<dispute_id> ────────────────────────────────────────────────
@disputes_bp.route('/<dispute_id>', methods=['GET'])
@jwt_required()
def dispute_detail(dispute_id):
    """
    Detalle completo de una disputa.
    Solo accesible por las partes de la transacción o por un admin.
    """
    user_id = get_jwt_identity()
    dispute = DisputeService.get_dispute_detail(user_id, dispute_id)
    return dispute.to_dict(include_transaction=True), 200
