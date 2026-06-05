"""Auth routes — /api/v1/auth/*"""
from flask import Blueprint, request
from flask_jwt_extended import (
    create_access_token, create_refresh_token,
    jwt_required, get_jwt_identity,
)
from app.models.user import User
from app.core.database import db
from app.core.exceptions import ConflictError, AuthenticationError
from app.core.notifications import notify

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')


def _user_with_tokens(user):
    access_token = create_access_token(identity=user.id)
    refresh_token = create_refresh_token(identity=user.id)
    return {
        'id': user.id,
        'email': user.email,
        'full_name': user.full_name,
        'role': user.role,
        'kyc_verified': user.kyc_verified,
        'rating': user.rating,
        'avatar_url': user.avatar_url,
        'access_token': access_token,
        'refresh_token': refresh_token,
    }


@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json() or {}
    email = data.get('email', '').strip()
    password = data.get('password', '')

    if not email or not password:
        return {'error': {'code': 'MISSING_FIELDS', 'message': 'Email and password required'}}, 400

    if User.query.filter_by(email=email).first():
        raise ConflictError('Email already registered')

    user = User(
        email=email,
        full_name=data.get('full_name', ''),
        role=data.get('role', 'buyer'),
        phone=data.get('phone'),
    )
    user.set_password(password)
    db.session.add(user)
    db.session.commit()

    return _user_with_tokens(user), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json() or {}
    email = data.get('email', '').strip()
    password = data.get('password', '')

    if not email or not password:
        return {'error': {'code': 'MISSING_FIELDS', 'message': 'Email and password required'}}, 400

    user = User.query.filter_by(email=email).first()
    if not user or not user.check_password(password):
        raise AuthenticationError('Invalid email or password')
    if not user.is_active:
        raise AuthenticationError('Account is inactive')

    notify(
        user_id=user.id,
        type='login',
        title='Inicio de sesión exitoso',
        body='Se detectó un nuevo inicio de sesión en tu cuenta.',
    )
    db.session.commit()

    return _user_with_tokens(user), 200


@auth_bp.route('/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    user_id = get_jwt_identity()
    return {'access_token': create_access_token(identity=user_id)}, 200


@auth_bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    return {'message': 'Logged out'}, 200
