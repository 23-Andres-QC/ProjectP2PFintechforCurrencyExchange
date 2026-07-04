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
                # El comprador tiene la razon: no se completa solo, se presiona al
                # vendedor a liberar el pago el mismo (por el flujo normal de confirmar,
                # que ademas genera el recibo). Se deja la transaccion lista para que
                # el vendedor solo tenga que confirmar.
                txn.status = 'voucher_uploaded'
                notify(
                    user_id=txn.vendor_id,
                    type='dispute',
                    title='⚠️ Debes liberar esta operación con urgencia',
                    body=(
                        'Un administrador revisó la disputa y confirmó que el comprador '
                        'cumplió con el pago. Revisa tu bandeja de pendientes y confirma '
                        f'la operación de inmediato. Nota del admin: {resolution_note}'
                    ),
                    resource_id=txn.id,
                )
            else:
                prior_favour_vendor = DisputeRepository.count_resolved_favour_vendor(txn.id)
                if prior_favour_vendor == 0:
                    # Primera vez que el vendedor gana: se le da al comprador una
                    # segunda oportunidad de subir el comprobante correcto, en vez
                    # de cancelar de una.
                    txn.status = 'accepted'
                    notify(
                        user_id=txn.buyer_id,
                        type='dispute',
                        title='⚠️ Tu comprobante no fue validado — última oportunidad',
                        body=(
                            'Un administrador revisó tu disputa y no pudo confirmar tu pago. '
                            'Tienes una segunda oportunidad para subir el comprobante correcto '
                            f'antes de que la operación se cancele. Nota del admin: {resolution_note}'
                        ),
                        resource_id=txn.id,
                    )
                else:
                    # Segunda vez que el vendedor gana en la misma transaccion:
                    # se cancela de verdad y se restaura el saldo de la oferta.
                    DisputeService._restore_offer_amount(txn)
                    txn.status = 'cancelled'
                    notify(
                        user_id=txn.buyer_id,
                        type='dispute',
                        title='Operación cancelada',
                        body=(
                            'Tras dos intentos, no se pudo validar tu comprobante de pago. '
                            f'La operación fue cancelada. Nota del admin: {resolution_note}'
                        ),
                        resource_id=txn.id,
                    )
                    notify(
                        user_id=txn.vendor_id,
                        type='dispute',
                        title='Operación cancelada a tu favor',
                        body=(
                            'La disputa se resolvió a tu favor tras dos intentos fallidos del '
                            f'comprador. La operación fue cancelada. Nota del admin: {resolution_note}'
                        ),
                        resource_id=txn.id,
                    )

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
