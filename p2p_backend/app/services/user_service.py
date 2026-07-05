from datetime import datetime
from flask_jwt_extended import create_access_token, create_refresh_token
from app.core.database import db
from app.core.exceptions import ConflictError, AuthenticationError, NotFoundError
from app.models.user import User
from app.repositories.user_repository import UserRepository


class UserService:

    @staticmethod
    def register(email: str, password: str, full_name: str,
                 role: str = 'buyer', phone: str | None = None,
                 dni: str | None = None, terms_accepted: bool = False,
                 terms_url: str | None = None, terms_version: str | None = None) -> User:
        if not email or not password:
            from app.core.exceptions import ValidationError
            raise ValidationError('Email and password required')
        if not full_name or not full_name.strip():
            from app.core.exceptions import ValidationError
            raise ValidationError('Full name is required')
        if len(password) < 8:
            from app.core.exceptions import ValidationError
            raise ValidationError('Password must have at least 8 characters')
        if dni and (not dni.isdigit() or len(dni) != 8):
            from app.core.exceptions import ValidationError
            raise ValidationError('DNI must have exactly 8 digits')
        if not terms_accepted:
            from app.core.exceptions import ValidationError
            raise ValidationError('Terms and conditions must be accepted')
        if role == 'admin':
            from app.core.exceptions import ValidationError
            raise ValidationError('Admin accounts cannot be created from public registration')
        if role not in ('buyer', 'vendor'):
            role = 'buyer'

        if UserRepository.get_by_email(email):
            raise ConflictError('Email already registered')

        if dni and UserRepository.get_by_dni(dni):
            raise ConflictError('DNI already registered')

        user = UserRepository.create(
            email=email,
            full_name=full_name.strip(),
            password=password,
            role=role,
            phone=phone,
            dni=dni,
            terms_accepted=True,
            terms_url=terms_url,
            terms_version=terms_version or '2026-07-02',
            terms_accepted_at=datetime.utcnow(),
        )
        user.kyc_status = 'approved'
        user.kyc_verified = True
        db.session.commit()
        return user

    @staticmethod
    def login(email: str, password: str) -> User:
        if not email or not password:
            from app.core.exceptions import ValidationError
            raise ValidationError('Email and password required')

        user = UserRepository.get_by_email(email)
        if not user or not user.check_password(password):
            raise AuthenticationError('Invalid email or password')
        if not user.is_active:
            raise AuthenticationError('Account is inactive')

        db.session.commit()
        return user

    @staticmethod
    def build_auth_response(user: User) -> dict:
        return {
            'id': user.id,
            'email': user.email,
            'full_name': user.full_name,
            'role': user.role,
            'kyc_status': user.kyc_status,
            'kyc_verified': user.kyc_verified,
            'rating': user.rating,
            'avatar_url': user.avatar_url,
            'access_token': create_access_token(identity=user.id),
            'refresh_token': create_refresh_token(identity=user.id),
        }

    @staticmethod
    def get_by_id(user_id: str) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')
        return user

    @staticmethod
    def update_profile(user_id: str, fields: dict) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        allowed = {k: v for k, v in fields.items() if k in ('full_name', 'phone', 'avatar_url')}
        UserRepository.update_fields(user, allowed)
        db.session.commit()
        return user

    @staticmethod
    def submit_kyc(user_id: str, document_urls: dict | None = None) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        document_urls = document_urls or {}
        if document_urls.get('dni_front_url'):
            user.dni_image_url = document_urls['dni_front_url']
        if document_urls.get('dni_back_url'):
            user.dni_back_url = document_urls['dni_back_url']
        if document_urls.get('selfie_url'):
            user.selfie_url = document_urls['selfie_url']
        if document_urls.get('signature_url'):
            user.signature_url = document_urls['signature_url']

        user.kyc_status = 'approved'
        user.kyc_verified = True
        db.session.commit()
        return user

    @staticmethod
    def save_signature(user_id: str, signature_url: str) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        user.signature_url = signature_url
        db.session.commit()
        return user

    @staticmethod
    def review_kyc(user_id: str, approved: bool, note: str | None = None) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        user.kyc_status = 'approved' if approved else 'rejected'
        user.kyc_verified = approved
        db.session.commit()
        return user

    @staticmethod
    def get_public_profile(user_id: str) -> dict:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')
        return {
            'id': user.id,
            'full_name': user.full_name,
            'avatar_url': user.avatar_url,
            'rating': user.rating,
            'total_transactions': user.completed_transactions_count(),
            'role': user.role,
            'kyc_status': user.kyc_status,
            'kyc_verified': user.kyc_verified,
        }
