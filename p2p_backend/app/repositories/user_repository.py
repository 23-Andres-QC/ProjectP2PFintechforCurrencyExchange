from app.core.database import db
from app.models.user import User


class UserRepository:

    @staticmethod
    def get_by_id(user_id: str) -> User | None:
        return db.session.get(User, user_id)

    @staticmethod
    def get_by_ids(user_ids: list[str]):
        if not user_ids:
            return []
        return User.query.filter(User.id.in_(user_ids)).all()

    @staticmethod
    def get_by_email(email: str) -> User | None:
        return User.query.filter_by(email=email).first()

    @staticmethod
    def get_by_dni(dni: str) -> User | None:
        return User.query.filter_by(dni=dni).first()

    @staticmethod
    def create(email: str, full_name: str, password: str,
               role: str = 'buyer', phone: str | None = None,
               dni: str | None = None) -> User:
        user = User(
            email=email,
            full_name=full_name,
            role=role,
            phone=phone,
            dni=dni,
        )
        user.set_password(password)
        db.session.add(user)
        return user

    @staticmethod
    def update_fields(user: User, fields: dict) -> User:
        for key, value in fields.items():
            setattr(user, key, value)
        return user

    @staticmethod
    def get_paginated(page: int = 1, per_page: int = 20,
                      role: str | None = None, active: bool | None = None):
        query = User.query
        if role:
            query = query.filter_by(role=role)
        if active is not None:
            query = query.filter_by(is_active=active)
        return query.order_by(User.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

    @staticmethod
    def count_user_disputes(user_id: str) -> int:
        from sqlalchemy import func
        from app.models.dispute import Dispute
        from app.models import Transaction
        return (
            db.session.query(func.count(Dispute.id))
            .join(Transaction, Dispute.transaction_id == Transaction.id)
            .filter(
                (Transaction.buyer_id == user_id) |
                (Transaction.vendor_id == user_id)
            ).scalar() or 0
        )
