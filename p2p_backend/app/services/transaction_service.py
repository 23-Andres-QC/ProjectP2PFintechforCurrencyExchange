import threading
from datetime import datetime

from app.core.database import db
from app.core.email import send_voucher_email
from app.core.exceptions import NotFoundError, AuthorizationError, AppException
from app.core.notifications import notify
from app.core.receipt_pdf import build_receipt_pdf
from app.core.storage import upload_receipt_pdf
from app.models import Transaction
from app.repositories.transaction_repository import TransactionRepository
from app.repositories.offer_repository import OfferRepository
from app.repositories.user_repository import UserRepository
from app.services.dispute_service import DisputeService


class TransactionService:

    @staticmethod
    def list_for_user(user_id: str, status: str | None = None) -> list[dict]:
        txns = TransactionRepository.get_by_user(user_id, status)
        return TransactionService._to_dict_many(txns)

    @staticmethod
    def pending_for_vendor(user_id: str) -> list[dict]:
        txns = TransactionRepository.get_pending_for_vendor(user_id)
        return TransactionService._to_dict_many(txns)

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
        offer = OfferRepository.get_by_id_for_update(data.get('offer_id'))
        if not offer or offer.status != 'active':
            raise AppException('OFFER_UNAVAILABLE', 'Offer not available', 400)
        if offer.vendor_id == user_id:
            raise AppException('OWN_OFFER', 'Cannot buy your own offer', 400)
        buyer = UserRepository.get_by_id(user_id)
        if not buyer or not buyer.is_active or buyer.is_banned:
            raise AuthorizationError('Buyer is not active')
        if not buyer.kyc_verified:
            raise AuthorizationError('KYC approval is required to buy')
        if TransactionRepository.get_open_for_buyer_and_offer(user_id, offer.id):
            raise AppException(
                'DUPLICATE_TRANSACTION',
                'Ya tienes una transacción activa para esta oferta',
                409,
            )

        amount_from = float(data.get('amount_from') or 0)
        amount_to = round(amount_from * offer.price_per_unit, 2)

        is_complete = offer.offer_type in ('full', 'complete')
        if is_complete:
            if abs(amount_from - offer.available_amount) > 0.001:
                raise AppException('INVALID_AMOUNT', 'Must buy the full available amount', 400)
        else:
            if amount_from < offer.min_transaction:
                raise AppException(
                    'INVALID_AMOUNT',
                    f'Monto mínimo: {offer.min_transaction}',
                    400,
                )
            if offer.max_transaction is not None and amount_from > offer.max_transaction:
                raise AppException(
                    'INVALID_AMOUNT',
                    f'Monto máximo: {offer.max_transaction}',
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

        db.session.flush()

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
    def upload_voucher(user_id: str, txn_id: str, data: dict, image_bytes: bytes, uploader) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')

        if user_id == txn.buyer_id:
            role = 'buyer'
        elif user_id == txn.vendor_id:
            role = 'seller'
        else:
            raise AuthorizationError('No eres parte de esta transacción')

        if role == 'buyer' and txn.status not in ('pending', 'accepted'):
            raise AppException(
                'INVALID_STATE', f'Cannot upload buyer voucher from status: {txn.status}', 400
            )
        if role == 'seller' and txn.status != 'voucher_uploaded':
            raise AppException(
                'INVALID_STATE', f'Cannot upload from status: {txn.status}', 400
            )

        user = UserRepository.get_by_id(user_id)
        user_email = user.email if user else user_id
        image_url = uploader(image_bytes, user_email, txn.id, role)

        voucher = TransactionRepository.add_voucher(
            transaction_id=txn.id,
            sender_id=user_id,
            image_url=image_url,
            description=data.get('description'),
        )

        if role == 'buyer':
            txn.status = 'voucher_uploaded'
            notify(
                user_id=txn.vendor_id,
                type='voucher',
                title='Comprobante de pago subido',
                body='El comprador subió su comprobante. Por favor revísalo y confirma la transacción.',
                resource_id=txn.id,
            )
        else:
            # Vendedor sube boleta: imagen guardada en Supabase y registrada.
            # El status permanece en voucher_uploaded para poder confirmar luego.
            txn.vendor_voucher_url = image_url
            notify(
                user_id=txn.buyer_id,
                type='voucher',
                title='Comprobante del vendedor subido',
                body='El vendedor subió su comprobante de envío. Revisa y cierra la transacción.',
                resource_id=txn.id,
            )

        db.session.commit()
        return {
            'id': voucher.id,
            'role': role,
            'image_url': image_url,
            'transaction_status': txn.status,
        }

    @staticmethod
    def list_vouchers(user_id: str) -> list[dict]:
        vouchers = TransactionRepository.get_vouchers_by_user(user_id)
        txns = {
            t.id: t for t in
            TransactionRepository.get_by_ids([v.transaction_id for v in vouchers])
        }
        senders = {
            u.id: u for u in
            UserRepository.get_by_ids([v.sender_id for v in vouchers])
        }
        result = []
        for v in vouchers:
            txn = txns.get(v.transaction_id)
            sender = senders.get(v.sender_id)
            result.append({
                'id': v.id,
                'transaction_id': v.transaction_id,
                'sender_name': sender.full_name if sender else None,
                'sender_email': sender.email if sender else None,
                'image_url': v.image_url,
                'description': v.description,
                'status': v.status,
                'created_at': v.created_at.isoformat() if v.created_at else None,
                'amount_from': txn.amount_from if txn else None,
                'amount_to': txn.amount_to if txn else None,
                'exchange_rate': txn.exchange_rate if txn else None,
                'transaction_status': txn.status if txn else None,
            })
        return result

    @staticmethod
    def confirm(user_id: str, txn_id: str) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.vendor_id != user_id:
            raise AuthorizationError('Only vendor can confirm')
        if txn.status != 'voucher_uploaded':
            raise AppException('INVALID_STATE', f'Cannot confirm from {txn.status}', 400)

        buyer_voucher = TransactionRepository.get_latest_voucher(txn.id, txn.buyer_id)
        seller_voucher = TransactionRepository.get_latest_voucher(txn.id, txn.vendor_id)
        if not buyer_voucher:
            raise AppException('MISSING_BUYER_VOUCHER', 'Buyer voucher is required before confirmation', 400)
        if not (seller_voucher or txn.vendor_voucher_url):
            raise AppException('MISSING_SELLER_VOUCHER', 'Seller voucher is required before confirmation', 400)

        txn.status = 'completed'
        txn.confirmed_at = datetime.utcnow()
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

        receipt_payload = TransactionService._to_dict(txn)
        receipt_payload['status'] = 'completed'
        pdf_bytes = build_receipt_pdf(receipt_payload)
        if buyer:
            txn.receipt_pdf_url = upload_receipt_pdf(pdf_bytes, buyer.email, txn.id)
            threading.Thread(
                target=send_voucher_email,
                kwargs={
                    'to_email': buyer.email,
                    'buyer_name': buyer.full_name,
                    'transaction_id': txn.id,
                    'pdf_url': txn.receipt_pdf_url,
                    'pdf_bytes': pdf_bytes,
                },
                daemon=True,
            ).start()

        db.session.commit()
        return {
            'message': 'Transaction completed',
            'status': 'completed',
            'receipt_pdf_url': txn.receipt_pdf_url or '',
        }

    @staticmethod
    def open_dispute(user_id: str, txn_id: str, data: dict,
                     image_bytes: bytes | None = None, uploader=None) -> dict:
        evidence_url = None
        if image_bytes is not None and uploader is not None:
            user = UserRepository.get_by_id(user_id)
            user_email = user.email if user else user_id
            evidence_url = uploader(image_bytes, user_email, txn_id)

        dispute = DisputeService.open_dispute(
            user_id=user_id,
            transaction_id=txn_id,
            reason=data.get('reason', 'payment_not_received'),
            description=data.get('description'),
            evidence_url=evidence_url,
        )

        db.session.commit()
        return {
            'id': dispute.id,
            'status': 'open',
            'transaction_status': 'disputed',
            'evidence_url': evidence_url,
        }

    @staticmethod
    def update_status(user_id: str, txn_id: str, new_status: str) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')

        if new_status == 'accepted':
            if txn.vendor_id != user_id:
                raise AuthorizationError('Only vendor can accept the transaction')
            if txn.status != 'pending':
                raise AppException('INVALID_STATE', f'Cannot accept from {txn.status}', 400)
            txn.status = 'accepted'
            txn.accepted_at = datetime.utcnow()
            db.session.commit()
            return TransactionService._to_dict(txn)

        if new_status == 'closed':
            if txn.status not in ('completed', 'closed'):
                raise AppException('INVALID_STATE', 'Can only close completed transactions', 400)
            if txn.buyer_id != user_id:
                raise AuthorizationError('Only buyer can close the transaction')
        elif new_status in ('cancelled', 'paused'):
            if txn.status in ('completed', 'closed'):
                raise AppException(
                    'INVALID_STATE', f'Cannot {new_status} a {txn.status} transaction', 400
                )
        else:
            raise AppException('INVALID_STATUS', 'Status must be cancelled or paused', 400)

        if new_status == 'cancelled':
            TransactionService._restore_offer_amount(txn)

        txn.status = new_status
        db.session.commit()
        return TransactionService._to_dict(txn)

    @staticmethod
    def _restore_offer_amount(txn: Transaction) -> None:
        if txn.status not in ('pending', 'accepted', 'voucher_uploaded', 'disputed'):
            return
        offer = OfferRepository.get_by_id_for_update(txn.offer_id)
        if not offer:
            return
        restored = min((offer.available_amount or 0) + txn.amount_from, offer.amount)
        offer.available_amount = restored
        if offer.status == 'closed' and offer.available_amount > 0:
            offer.status = 'active'

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
    def upload_vendor_voucher(user_id: str, txn_id: str, data: dict, image_bytes: bytes | None = None, uploader=None) -> dict:
        txn = TransactionRepository.get_by_id(txn_id)
        if not txn:
            raise NotFoundError('Transaction not found')
        if txn.vendor_id != user_id:
            raise AuthorizationError('Only vendor can upload vendor voucher')
        if txn.status != 'voucher_uploaded':
            raise AppException('INVALID_STATE', f'Cannot upload from status: {txn.status}', 400)

        image_url = data.get('image_url', '')
        if not image_url and image_bytes is not None and uploader is not None:
            user = UserRepository.get_by_id(user_id)
            user_email = user.email if user else user_id
            image_url = uploader(image_bytes, user_email, txn.id, 'seller')
            TransactionRepository.add_voucher(
                transaction_id=txn.id,
                sender_id=user_id,
                image_url=image_url,
                description=data.get('description') or 'Comprobante del vendedor',
            )
        if not image_url:
            raise AppException('MISSING_FIELD', 'image_url is required', 400)

        txn.vendor_voucher_url = image_url
        db.session.commit()
        return {'message': 'Vendor voucher saved', 'url': image_url}
    
    @staticmethod
    def _to_dict(t: Transaction) -> dict:
        buyer = UserRepository.get_by_id(t.buyer_id)
        vendor = UserRepository.get_by_id(t.vendor_id)
        buyer_voucher = TransactionRepository.get_latest_voucher(t.id, t.buyer_id)
        seller_voucher = TransactionRepository.get_latest_voucher(t.id, t.vendor_id)
        return TransactionService._transaction_dict(t, buyer, vendor, buyer_voucher, seller_voucher)

    @staticmethod
    def _to_dict_many(transactions: list[Transaction]) -> list[dict]:
        if not transactions:
            return []

        user_ids = {t.buyer_id for t in transactions} | {t.vendor_id for t in transactions}
        users = {u.id: u for u in UserRepository.get_by_ids(list(user_ids))}
        vouchers = TransactionRepository.get_latest_vouchers_for_transactions([t.id for t in transactions])

        return [
            TransactionService._transaction_dict(
                t,
                users.get(t.buyer_id),
                users.get(t.vendor_id),
                vouchers.get((t.id, t.buyer_id)),
                vouchers.get((t.id, t.vendor_id)),
            )
            for t in transactions
        ]

    @staticmethod
    def _transaction_dict(t: Transaction, buyer, vendor, buyer_voucher, seller_voucher) -> dict:
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
            'created_at': t.created_at.isoformat() if t.created_at else None,
            'updated_at': t.updated_at.isoformat() if t.updated_at else None,
            'buyer_voucher_url': buyer_voucher.image_url if buyer_voucher else None,
            'seller_voucher_url': seller_voucher.image_url if seller_voucher else t.vendor_voucher_url,
            'vendor_voucher_url': t.vendor_voucher_url,
            'receipt_pdf_url': t.receipt_pdf_url,
            'accepted_at': t.accepted_at.isoformat() if getattr(t, 'accepted_at', None) else None,
            'confirmed_at': t.confirmed_at.isoformat() if getattr(t, 'confirmed_at', None) else None,
        }
    
