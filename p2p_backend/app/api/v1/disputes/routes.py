from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from app.services.dispute_service import DisputeService

disputes_bp = Blueprint('disputes', __name__, url_prefix='/disputes')


def _paginate_params():
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))
    return page, per_page


@disputes_bp.route('/my-disputes', methods=['GET'])
@jwt_required()
def my_disputes():
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


@disputes_bp.route('/<dispute_id>', methods=['GET'])
@jwt_required()
def dispute_detail(dispute_id):
    user_id = get_jwt_identity()
    dispute = DisputeService.get_dispute_detail(user_id, dispute_id)
    return dispute.to_dict(include_transaction=True), 200
