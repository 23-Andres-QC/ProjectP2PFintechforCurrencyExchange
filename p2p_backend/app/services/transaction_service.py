from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, AppException
from app.core.notifications import notify
from app.models import Transaction
from app.repositories.transaction_repository import TransactionRepository
from app.repositories.offer_repository import OfferRepository
from app.repositories.user_repository import UserRepository
from app.services.dispute_service import DisputeService


class TransactionService:

    @staticmethod
    def list_for_user(user_id: str, status: str | None = None) -> list[dict]:
        txns = TransactionRepository.get_by_user(user_id, status)
        return [TransactionService._to_dict(t) for t in txns]

    @staticmethod
    def pending_for_vendor(user_id: str) -> list[dict]:
        txns = TransactionRepository.get_pending_for_vendor(user_id)
        return [TransactionService._to_dict(t) for t in txns]

    @staticmethod
    def get(user_id: str, txn_id: str) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')
        return TransactionService._to_dict(txn)

    @staticmethod
    def create(user_id: str, data: dict) -> dict:
        offer = OfferRepository.get_by_id(data.get('offer_id'))
        if not offer or offer.status != 'active':
            raise AppException('OFFER_UNAVAILABLE', 'Offer not available', 400)
        if offer.vendor_id == user_id:
            raise AppException('OWN_OFFER', 'Cannot buy your own offer', 400)

        amount_from = data.get('amount_from', 0)
        amount_to = data.get('amount_to', 0)

        if offer.offer_type == 'full':
            if amount_from != offer.available_amount:
                raise AppException('INVALID_AMOUNT', 'Must buy the full available amount', 400)
        else:
            if amount_from < offer.min_transaction or amount_from > offer.max_transaction:
                raise AppException(
                    'INVALID_AMOUNT',
                    f'Amount must be between {offer.min_transaction} and {offer.max_transaction}',
                    400,
                )
            if amount_from > offer.available_amount:
                raise AppException('INVALID_AMOUNT', 'Amount exceeds available amount', 400)

        txn = TransactionRepository.create(
            offer_id=offer.id,
            buyer_id=user_id,
            vendor_id=offer.vendor_id,
            amount_from=amount_from,
            amount_to=amount_to,
            exchange_rate=offer.price_per_unit,
            buyer_payment_account=data.get('buyer_payment_account'),
            vendor_payment_account=data.get('vendor_payment_account'),
        )

        offer.available_amount -= amount_from
        if offer.available_amount <= 0:
            offer.status = 'closed'

        notify(
            user_id=txn.buyer_id,
            type='transaction',
            title='Transacción creada',
            body='Tu solicitud de cambio está pendiente de confirmación por el vendedor.',
            resource_id=txn.id,
        )
        notify(
            user_id=txn.vendor_id,
            type='transaction',
            title='Nueva transacción pendiente',
            body=f'Un comprador inició una transacción por {txn.amount_from} {offer.from_currency}. Revisa y confirma.',
            resource_id=txn.id,
        )

        db.session.commit()
        return TransactionService._to_dict(txn)

    @staticmethod
    def upload_voucher(user_id: str, txn_id: str, data: dict) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.buyer_id != user_id:
            raise AuthorizationError('Only buyer can upload voucher')

        voucher = TransactionRepository.add_voucher(
            transaction_id=txn.id,
            sender_id=user_id,
            image_url=data.get('image_url', ''),
            description=data.get('description'),
        )
        txn.status = 'voucher_uploaded'

        notify(
            user_id=txn.vendor_id,
            type='voucher',
            title='Comprobante de pago subido',
            body='El comprador subió su comprobante. Por favor revísalo y confirma la transacción.',
            resource_id=txn.id,
        )

        db.session.commit()
        return {'id': voucher.id, 'status': 'pending', 'transaction_status': 'voucher_uploaded'}

    @staticmethod
    def confirm(user_id: str, txn_id: str) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.vendor_id != user_id:
            raise AuthorizationError('Only vendor can confirm')
        if txn.status not in ('voucher_uploaded', 'pending'):
            raise AppException('INVALID_STATE', f'Cannot confirm from {txn.status}', 400)

        txn.status = 'completed'
        vendor = UserRepository.get_by_id(txn.vendor_id)
        buyer = UserRepository.get_by_id(txn.buyer_id)
        if vendor:
            vendor.total_transactions = (vendor.total_transactions or 0) + 1
        if buyer:
            buyer.total_transactions = (buyer.total_transactions or 0) + 1

        notify(
            user_id=txn.buyer_id,
            type='transaction',
            title='Transacción completada',
            body='El vendedor confirmó el pago. Tu transacción fue completada exitosamente.',
            resource_id=txn.id,
        )

        db.session.commit()
        return {'message': 'Transaction completed', 'status': 'completed'}

    @staticmethod
    def open_dispute(user_id: str, txn_id: str, data: dict) -> dict:
        dispute = DisputeService.open_dispute(
            user_id=user_id,
            transaction_id=txn_id,
            reason=data.get('reason', 'payment_not_received'),
            description=data.get('description'),
        )

        txn = TransactionRepository.get_by_id(txn_id)
        other_id = txn.vendor_id if user_id == txn.buyer_id else txn.buyer_id
        notify(
            user_id=other_id,
            type='dispute',
            title='Disputa abierta en tu transacción',
            body=f'Se abrió una disputa por motivo: {dispute.reason}. Un administrador revisará el caso.',
            resource_id=dispute.id,
        )
        db.session.commit()
        return {'id': dispute.id, 'status': 'open', 'transaction_status': 'disputed'}

    @staticmethod
    def update_status(user_id: str, txn_id: str, new_status: str) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')

        if new_status == 'closed':
            if txn.status != 'completed':
                raise AppException('INVALID_STATE', 'Can only close completed transactions', 400)
            if txn.buyer_id != user_id:
                raise AuthorizationError('Only buyer can close the transaction')
        elif new_status not in ('cancelled', 'paused'):
            raise AppException('INVALID_STATUS', 'Status must be cancelled or paused', 400)

        txn.status = new_status
        db.session.commit()
        return TransactionService._to_dict(txn)

    @staticmethod
    def list_disputes(user_id: str) -> list[dict]:
        disputes = TransactionRepository.get_user_disputes(user_id)
        return [
            {
                'id': d.id,
                'transaction_id': d.transaction_id,
                'initiator_id': d.initiator_id,
                'reason': d.reason,
                'description': d.description,
                'status': d.status,
                'created_at': d.created_at.isoformat(),
            }
            for d in disputes
        ]
    
    @staticmethod
    def upload_vendor_voucher(user_id: str, txn_id: str, data: dict) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.vendor_id != user_id:
            raise AuthorizationError('Only vendor can upload vendor voucher')
        if txn.status != 'voucher_uploaded':
            raise AppException('INVALID_STATE', f'Cannot upload from status: {txn.status}', 400)

        image_url = data.get('image_url', '')
        if not image_url:
            raise AppException('MISSING_FIELD', 'image_url is required', 400)

        txn.vendor_voucher_url = image_url
        db.session.commit()
        return {'message': 'Vendor voucher saved', 'url': image_url}
    
    @staticmethod
    def _to_dict(t: Transaction) -> dict:
        buyer = UserRepository.get_by_id(t.buyer_id)
        vendor = UserRepository.get_by_id(t.vendor_id)
        return {
            'id': t.id,
            'offer_id': t.offer_id,
            'buyer_id': t.buyer_id,
            'vendor_id': t.vendor_id,
            'buyer_name': buyer.full_name if buyer else None,
            'vendor_name': vendor.full_name if vendor else None,
            'amount_from': t.amount_from,
            'amount_to': t.amount_to,
            'exchange_rate': t.exchange_rate,
            'status': t.status,
            'buyer_payment_account': t.buyer_payment_account,
            'vendor_payment_account': t.vendor_payment_account,
            'created_at': t.created_at.isoformat(),
            'updated_at': t.updated_at.isoformat() if t.updated_at else None,
            'vendor_voucher_url': t.vendor_voucher_url, 
        }
    
