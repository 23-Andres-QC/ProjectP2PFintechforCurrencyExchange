from app.core.database import db
from app.core.exceptions import (
    AppException, NotFoundError, AuthorizationError, ConflictError
)
from app.core.notifications import notify
from app.models import Transaction
from app.models.dispute import Dispute
from app.models.user import User
from app.repositories.dispute_repository import DisputeRepository
from app.repositories.offer_repository import OfferRepository


class DisputeService:

    @staticmethod
    def open_dispute(user_id: str, transaction_id: str,
                     reason: str, description: str | None = None,
                     evidence_url: str | None = None) -> Dispute:
        txn: Transaction | None = db.session.get(Transaction, transaction_id)
        if not txn:
            raise NotFoundError('Transaction not found')

        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')

        existing = DisputeRepository.get_by_transaction(transaction_id)
        if existing:
            raise ConflictError('There is already an open dispute for this transaction')

        if txn.status not in ('pending', 'accepted', 'voucher_uploaded'):
            raise AppException(
                'INVALID_STATE',
                f'Cannot dispute a transaction with status {txn.status}',
                400,
            )

        if reason not in Dispute.VALID_REASONS:
            raise AppException('INVALID_REASON',
                               f'Reason must be one of: {", ".join(Dispute.VALID_REASONS)}', 400)

        dispute = DisputeRepository.create(
            transaction_id=transaction_id,
            initiator_id=user_id,
            reason=reason,
            description=description,
            evidence_url=evidence_url,
        )
        txn.status = 'disputed'
        db.session.flush()
        DisputeService._notify_admins_new_dispute(dispute, txn, reason)
        db.session.commit()
        return dispute

    @staticmethod
    def get_my_disputes(user_id: str, page: int = 1, per_page: int = 20):
        return DisputeRepository.get_by_user(user_id, page, per_page)

    @staticmethod
    def get_dispute_detail(user_id: str, dispute_id: str) -> Dispute:
        dispute = DisputeRepository.get_by_id(dispute_id)
        if not dispute:
            raise NotFoundError('Dispute not found')

        user: User | None = db.session.get(User, user_id)
        txn: Transaction = dispute.transaction

        is_admin = user and user.role == 'admin'
        is_party = txn and (txn.buyer_id == user_id or txn.vendor_id == user_id)

        if not is_admin and not is_party:
            raise AuthorizationError('Access denied')

        return dispute

    @staticmethod
    def list_disputes_admin(page: int = 1, per_page: int = 20,
                            status: str | None = None):
        return DisputeRepository.get_all(page, per_page, status)

    @staticmethod
    def take_dispute(admin_id: str, dispute_id: str) -> Dispute:
        dispute = DisputeRepository.get_by_id(dispute_id)
        if not dispute:
            raise NotFoundError('Dispute not found')

        if dispute.status not in ('open', 'under_review'):
            raise AppException('INVALID_STATE',
                               f'Cannot take dispute with status {dispute.status}', 400)

        DisputeRepository.set_under_review(dispute, admin_id)
        db.session.commit()
        return dispute

    @staticmethod
    def resolve_dispute(admin_id: str, dispute_id: str,
                        resolution: str, resolution_note: str | None = None) -> Dispute:
        if resolution not in ('favour_buyer', 'favour_vendor'):
            raise AppException('INVALID_RESOLUTION',
                               "resolution must be 'favour_buyer' or 'favour_vendor'", 400)

        if not resolution_note or not resolution_note.strip():
            raise AppException('MISSING_FIELD', 'resolution_note is required', 400)

        dispute = DisputeRepository.get_by_id(dispute_id)
        if not dispute:
            raise NotFoundError('Dispute not found')

        if dispute.status == 'resolved':
            raise ConflictError('Dispute is already resolved')

        if dispute.status == 'closed':
            raise AppException('INVALID_STATE', 'Cannot resolve a closed dispute', 400)

        txn: Transaction = dispute.transaction
        if txn:
            if resolution == 'favour_buyer':
                # El pago del comprador es valido: la operacion vuelve al turno
                # del vendedor, que debe depositar al comprador y confirmar.
                txn.status = 'voucher_uploaded'
                notify(
                    user_id=txn.vendor_id,
                    type='dispute',
                    title='Deposita al comprador cuanto antes',
                    body=(
                        'Un administrador resolvio la disputa a favor del comprador: '
                        'su pago es valido. Apresurate a realizar el deposito al comprador, '
                        'sube tu comprobante y confirma la operacion. '
                        f'Nota del admin: {resolution_note}'
                    ),
                    resource_id=txn.id,
                )
                notify(
                    user_id=txn.buyer_id,
                    type='dispute',
                    title='Disputa resuelta a tu favor',
                    body=(
                        'El vendedor fue notificado para realizar tu deposito lo antes posible. '
                        'Recibiras una notificacion cuando suba su comprobante. '
                        f'Nota del admin: {resolution_note}'
                    ),
                    resource_id=txn.id,
                )
            else:
                # El voucher del comprador no es valido o el dinero no llego:
                # la operacion vuelve a 'accepted' para que el comprador pueda
                # subir un nuevo comprobante (segunda oportunidad).
                txn.status = 'accepted'
                notify(
                    user_id=txn.buyer_id,
                    type='dispute',
                    title='Tu comprobante no es valido',
                    body=(
                        'Un administrador resolvio la disputa a favor del vendedor: '
                        'el voucher enviado no es valido o el dinero no llego. '
                        'Tienes una segunda oportunidad: realiza el pago correctamente '
                        'y sube un nuevo comprobante desde la operacion. '
                        f'Nota del admin: {resolution_note}'
                    ),
                    resource_id=txn.id,
                )
                notify(
                    user_id=txn.vendor_id,
                    type='dispute',
                    title='Disputa resuelta a tu favor',
                    body=(
                        'El comprador fue notificado de que su comprobante no es valido '
                        'y debe intentar el pago de nuevo. Espera su nuevo voucher. '
                        f'Nota del admin: {resolution_note}'
                    ),
                    resource_id=txn.id,
                )

            DisputeRepository.resolve(dispute, admin_id, resolution, resolution_note)
            db.session.commit()
            return dispute


        DisputeRepository.resolve(dispute, admin_id, resolution, resolution_note)
        db.session.commit()
        return dispute

    @staticmethod
    def _restore_offer_amount(txn: Transaction) -> None:
        offer = OfferRepository.get_by_id_for_update(txn.offer_id)
        if not offer:
            return
        offer.available_amount = min((offer.available_amount or 0) + txn.amount_from, offer.amount)
        if offer.status == 'closed' and offer.available_amount > 0:
            offer.status = 'active'

    @staticmethod
    def _notify_admins_new_dispute(dispute: Dispute, txn: Transaction, reason: str) -> None:
        admins = User.query.filter_by(
            role='admin',
            is_active=True,
            is_banned=False,
        ).all()

        for admin in admins:
            notify(
                user_id=admin.id,
                # 'admin' y no 'dispute': el resource_id es el id de la disputa
                # (no de la transaccion) y el cliente lo enruta al panel admin.
                type='admin',
                title='Nueva disputa abierta',
                body=(
                    f'Se abrio una disputa para la transaccion {txn.id[:8].upper()}. '
                    f'Motivo: {reason}.'
                ),
                resource_id=dispute.id,
            )
