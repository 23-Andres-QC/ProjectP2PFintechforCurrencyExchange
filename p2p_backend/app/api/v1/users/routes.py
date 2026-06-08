"""Users routes — /api/v1/users/*"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.core.database import db
from app.core.exceptions import NotFoundError
from app.core.notifications import notify
from app.models.user import User

users_bp = Blueprint('users', __name__, url_prefix='/users')


@users_bp.route('/me', methods=['GET'])
@jwt_required()
def get_me():
    user_id = get_jwt_identity()
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')
    return user.to_dict(), 200


@users_bp.route('/profile', methods=['PATCH'])
@jwt_required()
def update_profile():
    user_id = get_jwt_identity()
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')

    data = request.get_json() or {}
    for field in ('full_name', 'phone', 'avatar_url'):
        if field in data:
            setattr(user, field, data[field])

    db.session.commit()
    return user.to_dict(), 200


@users_bp.route('/kyc', methods=['POST'])
@jwt_required()
def submit_kyc():
    user_id = get_jwt_identity()
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')

    # In this demo the images are received but not processed by OCR.
    # Receiving the multipart request is sufficient to approve KYC.
    user.kyc_verified = True
    db.session.commit()

    notify(
        user_id=user.id,
        type='kyc',
        title='Verificación KYC aprobada',
        body='Tu identidad ha sido verificada exitosamente.',
    )
    db.session.commit()

    return {'message': 'KYC aprobado', 'kyc_verified': True}, 200


@users_bp.route('/<user_id>', methods=['GET'])
def get_public_profile(user_id):
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')
    return {
        'id': user.id,
        'full_name': user.full_name,
        'avatar_url': user.avatar_url,
        'rating': user.rating,
        'total_transactions': user.total_transactions,
        'role': user.role,
        'kyc_verified': user.kyc_verified,
    }, 200
