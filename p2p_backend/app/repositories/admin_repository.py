from sqlalchemy import func
from app.core.database import db
from app.models import Transaction, Dispute
from app.models.user import User


class AdminRepository:

    @staticmethod
    def get_dashboard_stats() -> dict:
        total_users = User.query.count()
        active_users = User.query.filter_by(is_active=True).count()
        total_txns = Transaction.query.count()
        completed_txns = Transaction.query.filter_by(status='completed').count()
        pending_disputes = Dispute.query.filter(
            Dispute.status.in_(('open', 'under_review'))
        ).count()
        resolved_disputes = Dispute.query.filter_by(status='resolved').count()
        total_volume = db.session.query(
            func.sum(Transaction.amount_to)
        ).filter_by(status='completed').scalar() or 0

        return {
            'users': {
                'total': total_users,
                'active': active_users,
            },
            'transactions': {
                'total': total_txns,
                'completed': completed_txns,
            },
            'disputes': {
                'pending': pending_disputes,
                'resolved': resolved_disputes,
            },
            'total_volume': float(total_volume),
        }

    @staticmethod
    def get_users_paginated(page: int = 1, per_page: int = 20,
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
    def get_user_dispute_count(user_id: str) -> int:
        return (
            db.session.query(func.count(Dispute.id))
            .join(Transaction, Dispute.transaction_id == Transaction.id)
            .filter(
                (Transaction.buyer_id == user_id) |
                (Transaction.vendor_id == user_id)
            ).scalar() or 0
        )
