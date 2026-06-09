from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.user_service import UserService

users_bp = Blueprint('users', __name__, url_prefix='/users')


@users_bp.route('/me', methods=['GET'])
@jwt_required()
def get_me():
    user_id = get_jwt_identity()
    user = UserService.get_by_id(user_id)
    return user.to_dict(), 200


@users_bp.route('/profile', methods=['PATCH'])
@jwt_required()
def update_profile():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    user = UserService.update_profile(user_id, data)
    return user.to_dict(), 200


@users_bp.route('/kyc', methods=['POST'])
@jwt_required()
def submit_kyc():
    user_id = get_jwt_identity()
    UserService.submit_kyc(user_id)
    return {'message': 'KYC aprobado', 'kyc_verified': True}, 200


@users_bp.route('/<user_id>', methods=['GET'])
def get_public_profile(user_id):
    return UserService.get_public_profile(user_id), 200
