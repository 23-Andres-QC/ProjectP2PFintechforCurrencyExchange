from flask_jwt_extended import create_access_token, create_refresh_token
from app.core.database import db
from app.core.exceptions import ConflictError, AuthenticationError, NotFoundError
from app.core.notifications import notify
from app.models.user import User
from app.repositories.user_repository import UserRepository


class UserService:

    @staticmethod
    def register(email: str, password: str, full_name: str,
                 role: str = 'buyer', phone: str | None = None,
                 dni: str | None = None) -> User:
        if not email or not password:
            from app.core.exceptions import ValidationError
            raise ValidationError('Email and password required')

        if UserRepository.get_by_email(email):
            raise ConflictError('Email already registered')

        if dni and UserRepository.get_by_dni(dni):
            raise ConflictError('DNI already registered')

        user = UserRepository.create(
            email=email,
            full_name=full_name,
            password=password,
            role=role,
            phone=phone,
            dni=dni,
        )
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

        notify(
            user_id=user.id,
            type='login',
            title='Inicio de sesión exitoso',
            body='Se detectó un nuevo inicio de sesión en tu cuenta.',
        )
        db.session.commit()
        return user

    @staticmethod
    def build_auth_response(user: User) -> dict:
        return {
            'id': user.id,
            'email': user.email,
            'full_name': user.full_name,
            'role': user.role,
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
    def submit_kyc(user_id: str) -> User:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        user.kyc_verified = True
        notify(
            user_id=user.id,
            type='kyc',
            title='Verificación KYC aprobada',
            body='Tu identidad ha sido verificada exitosamente.',
        )
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
            'total_transactions': user.total_transactions,
            'role': user.role,
            'kyc_verified': user.kyc_verified,
        }
