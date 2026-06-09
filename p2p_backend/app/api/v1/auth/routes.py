from flask import Blueprint, request
from flask_jwt_extended import (
    create_access_token,
    jwt_required,
    get_jwt_identity,
)
from app.services.user_service import UserService

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')


@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json() or {}
    user = UserService.register(
        email=data.get('email', '').strip(),
        password=data.get('password', ''),
        full_name=data.get('full_name', ''),
        role=data.get('role', 'buyer'),
        phone=data.get('phone'),
        dni=data.get('dni', '').strip() or None,
    )
    return UserService.build_auth_response(user), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json() or {}
    user = UserService.login(
        email=data.get('email', '').strip(),
        password=data.get('password', ''),
    )
    return UserService.build_auth_response(user), 200


@auth_bp.route('/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    user_id = get_jwt_identity()
    return {'access_token': create_access_token(identity=user_id)}, 200


@auth_bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    return {'message': 'Logged out'}, 200
