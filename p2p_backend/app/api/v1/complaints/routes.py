from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.complaint_service import ComplaintService

complaints_bp = Blueprint('complaints', __name__, url_prefix='/complaints')


@complaints_bp.route('', methods=['POST'])
@jwt_required()
def create_complaint():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return ComplaintService.create(
        user_id=user_id,
        complaint_type=data.get('type'),
        description=data.get('description'),
    ), 201


@complaints_bp.route('/my-complaints', methods=['GET'])
@jwt_required()
def my_complaints():
    user_id = get_jwt_identity()
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))
    return ComplaintService.my_complaints(user_id, page, per_page), 200
