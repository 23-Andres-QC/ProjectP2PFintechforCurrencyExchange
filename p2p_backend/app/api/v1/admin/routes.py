from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.core.exceptions import AuthorizationError, AppException
from app.repositories.user_repository import UserRepository
from app.services.admin_service import AdminService

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')


def _require_admin():
    user_id = get_jwt_identity()
    user = UserRepository.get_by_id(user_id)
    if not user or user.role != 'admin':
        raise AuthorizationError('Admin access required')
    return user


def _paginate_params():
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))
    return page, per_page


@admin_bp.route('/dashboard', methods=['GET'])
@jwt_required()
def dashboard():
    _require_admin()
    return AdminService.get_dashboard(), 200


@admin_bp.route('/users', methods=['GET'])
@jwt_required()
def list_users():
    _require_admin()
    page, per_page = _paginate_params()
    role_filter = request.args.get('role')
    active_filter = request.args.get('active')
    active = (active_filter == '1') if active_filter is not None else None
    return AdminService.list_users(page, per_page, role_filter, active), 200


@admin_bp.route('/users/<user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    _require_admin()
    return AdminService.get_user(user_id), 200


@admin_bp.route('/users/<user_id>/ban', methods=['PATCH'])
@jwt_required()
def ban_user(user_id):
    admin = _require_admin()
    data = request.get_json() or {}
    banned = data.get('banned')
    if banned is None:
        raise AppException('MISSING_FIELD', '"banned" field is required', 400)
    return AdminService.ban_user(admin.id, user_id, bool(banned)), 200


@admin_bp.route('/users/<user_id>/kyc', methods=['PATCH'])
@jwt_required()
def review_user_kyc(user_id):
    _require_admin()
    data = request.get_json() or {}
    if 'approved' not in data:
        raise AppException('MISSING_FIELD', '"approved" field is required', 400)
    return AdminService.review_user_kyc(user_id, bool(data.get('approved')), data.get('note')), 200


@admin_bp.route('/disputes', methods=['GET'])
@jwt_required()
def list_disputes():
    _require_admin()
    page, per_page = _paginate_params()
    return AdminService.list_disputes(page, per_page, request.args.get('status')), 200


@admin_bp.route('/disputes/<dispute_id>', methods=['GET'])
@jwt_required()
def get_dispute(dispute_id):
    admin = _require_admin()
    dispute = AdminService.get_dispute(admin.id, dispute_id)
    return dispute.to_dict(include_transaction=True), 200


@admin_bp.route('/disputes/<dispute_id>/take', methods=['PATCH'])
@jwt_required()
def take_dispute(dispute_id):
    admin = _require_admin()
    return AdminService.take_dispute(admin.id, dispute_id), 200


@admin_bp.route('/disputes/<dispute_id>/resolve', methods=['PATCH'])
@jwt_required()
def resolve_dispute(dispute_id):
    admin = _require_admin()
    data = request.get_json() or {}
    resolution = data.get('resolution')
    if not resolution:
        raise AppException('MISSING_FIELD', '"resolution" field is required', 400)
    return AdminService.resolve_dispute(
        admin_id=admin.id,
        dispute_id=dispute_id,
        resolution=resolution,
        resolution_note=data.get('resolution_note'),
    ), 200


@admin_bp.route('/complaints', methods=['GET'])
@jwt_required()
def list_complaints():
    _require_admin()
    page, per_page = _paginate_params()
    return AdminService.list_complaints(page, per_page, request.args.get('status')), 200


@admin_bp.route('/complaints/<complaint_id>', methods=['GET'])
@jwt_required()
def get_complaint(complaint_id):
    _require_admin()
    return AdminService.get_complaint(complaint_id), 200


@admin_bp.route('/complaints/<complaint_id>/resolve', methods=['PATCH'])
@jwt_required()
def resolve_complaint(complaint_id):
    admin = _require_admin()
    data = request.get_json() or {}
    return AdminService.resolve_complaint(admin.id, complaint_id, data.get('admin_note', '')), 200
