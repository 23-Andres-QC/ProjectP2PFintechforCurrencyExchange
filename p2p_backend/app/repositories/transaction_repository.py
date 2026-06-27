from app.core.database import db
from app.models import Transaction, Voucher, Dispute


class TransactionRepository:

    @staticmethod
    def get_by_id(txn_id: str) -> Transaction | None:
        return db.session.get(Transaction, txn_id)

    @staticmethod
    def get_by_ids(txn_ids: list[str]):
        if not txn_ids:
            return []
        return Transaction.query.filter(Transaction.id.in_(txn_ids)).all()

    @staticmethod
    def get_by_user(user_id: str, status: str | None = None):
        query = Transaction.query.filter(
            (Transaction.buyer_id == user_id) | (Transaction.vendor_id == user_id)
        )
        if status:
            query = query.filter_by(status=status)
        return query.order_by(Transaction.created_at.desc()).all()

    @staticmethod
    def get_pending_for_vendor(vendor_id: str):
        return Transaction.query.filter(
            Transaction.vendor_id == vendor_id,
            Transaction.status.in_(('pending', 'accepted', 'voucher_uploaded'))
        ).order_by(Transaction.created_at.desc()).all()

    @staticmethod
    def create(offer_id: str, buyer_id: str, vendor_id: str,
               amount_from: float, amount_to: float, exchange_rate: float,
               buyer_payment_account: str | None = None,
               vendor_payment_account: str | None = None) -> Transaction:
        txn = Transaction(
            offer_id=offer_id,
            buyer_id=buyer_id,
            vendor_id=vendor_id,
            amount_from=amount_from,
            amount_to=amount_to,
            exchange_rate=exchange_rate,
            status='pending',
            buyer_payment_account=buyer_payment_account,
            vendor_payment_account=vendor_payment_account,
        )
        db.session.add(txn)
        return txn

    @staticmethod
    def add_voucher(transaction_id: str, sender_id: str,
                    image_url: str, description: str | None = None) -> Voucher:
        voucher = Voucher(
            transaction_id=transaction_id,
            sender_id=sender_id,
            image_url=image_url,
            description=description,
            status='pending',
        )
        db.session.add(voucher)
        return voucher

    @staticmethod
    def create_dispute(transaction_id: str, initiator_id: str,
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
    def get_user_disputes(user_id: str):
        return (
            db.session.query(Dispute)
            .join(Transaction, Dispute.transaction_id == Transaction.id)
            .filter(
                (Transaction.buyer_id == user_id) |
                (Transaction.vendor_id == user_id)
            ).all()
        )

    @staticmethod
    def get_vouchers_by_user(user_id: str):
        return (
            Voucher.query
            .filter_by(sender_id=user_id)
            .order_by(Voucher.created_at.desc())
            .all()
        )

    @staticmethod
    def get_latest_voucher(transaction_id: str, sender_id: str):
        return (
            Voucher.query
            .filter_by(transaction_id=transaction_id, sender_id=sender_id)
            .order_by(Voucher.created_at.desc())
            .first()
        )

    @staticmethod
    def get_latest_vouchers_for_transactions(transaction_ids: list[str]):
        if not transaction_ids:
            return {}

        latest = {}
        vouchers = (
            Voucher.query
            .filter(Voucher.transaction_id.in_(transaction_ids))
            .order_by(Voucher.created_at.desc())
            .all()
        )
        for voucher in vouchers:
            key = (voucher.transaction_id, voucher.sender_id)
            if key not in latest:
                latest[key] = voucher
        return latest

    @staticmethod
    def set_status(txn: Transaction, status: str) -> Transaction:
        txn.status = status
        return txn
