"""Complaints routes — /api/v1/complaints/*"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.core.database import db
from app.core.exceptions import AppException
from app.models.complaint import Complaint

complaints_bp = Blueprint('complaints', __name__, url_prefix='/complaints')


@complaints_bp.route('', methods=['POST'])
@jwt_required()
def create_complaint():
    user_id = get_jwt_identity()
    data = request.get_json() or {}

    complaint_type = data.get('type')
    description = data.get('description')

    if not complaint_type or complaint_type not in Complaint.VALID_TYPES:
        raise AppException('INVALID_TYPE',
                           f'Type must be one of: {", ".join(Complaint.VALID_TYPES)}', 400)
    if not description or not description.strip():
        raise AppException('MISSING_FIELD', 'description is required', 400)

    complaint = Complaint(
        user_id=user_id,
        type=complaint_type,
        description=description.strip()
    )
    db.session.add(complaint)
    db.session.commit()

    return complaint.to_dict(), 201


@complaints_bp.route('/my-complaints', methods=['GET'])
@jwt_required()
def my_complaints():
    user_id = get_jwt_identity()
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))

    pagination = Complaint.query.filter_by(user_id=user_id)\
        .order_by(Complaint.created_at.desc())\
        .paginate(page=page, per_page=per_page, error_out=False)

    return {
        'complaints': [c.to_dict() for c in pagination.items],
        'pagination': {
            'page':     pagination.page,
            'per_page': pagination.per_page,
            'total':    pagination.total,
            'pages':    pagination.pages,
            'has_next': pagination.has_next,
            'has_prev': pagination.has_prev,
        }
    }, 200