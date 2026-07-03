from datetime import datetime
from app.core.database import db
from app.models.dispute import Dispute
from app.models import Transaction


class DisputeRepository:

    @staticmethod
    def get_by_id(dispute_id: str) -> Dispute | None:
        return db.session.get(Dispute, dispute_id)

    @staticmethod
    def get_by_transaction(transaction_id: str) -> Dispute | None:
        return Dispute.query.filter(
            Dispute.transaction_id == transaction_id,
            Dispute.status.in_(('open', 'under_review'))
        ).first()

    @staticmethod
    def get_all_open(page: int = 1, per_page: int = 20):
        return Dispute.query.filter(
            Dispute.status.in_(('open', 'under_review'))
        ).order_by(Dispute.created_at.asc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

    @staticmethod
    def get_all(page: int = 1, per_page: int = 20, status: str | None = None):
        q = Dispute.query
        if status:
            q = q.filter_by(status=status)
        return q.order_by(Dispute.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

    @staticmethod
    def get_by_user(user_id: str, page: int = 1, per_page: int = 20):
        return (
            db.session.query(Dispute)
            .join(Transaction, Dispute.transaction_id == Transaction.id)
            .filter(
                (Transaction.buyer_id == user_id) |
                (Transaction.vendor_id == user_id)
            )
            .order_by(Dispute.created_at.desc())
            .paginate(page=page, per_page=per_page, error_out=False)
        )

    @staticmethod
    def create(transaction_id: str, initiator_id: str,
               reason: str, description: str | None = None) -> Dispute:
        dispute = Dispute(
            transaction_id=transaction_id,
            initiator_id=initiator_id,
            reason=reason,
            description=description,
            status='open',
        )
        db.session.add(dispute)
        return dispute

    @staticmethod
    def resolve(dispute: Dispute, admin_id: str,
                resolution: str, resolution_note: str | None = None) -> Dispute:
        dispute.status = 'resolved'
        dispute.resolved_by = admin_id
        dispute.resolution = resolution
        dispute.resolution_note = resolution_note
        dispute.resolved_at = datetime.utcnow()
        return dispute

    @staticmethod
    def set_under_review(dispute: Dispute, admin_id: str) -> Dispute:
        dispute.status = 'under_review'
        dispute.resolved_by = admin_id
        return dispute

    @staticmethod
    def close(dispute: Dispute) -> Dispute:
        dispute.status = 'closed'
        return dispute
