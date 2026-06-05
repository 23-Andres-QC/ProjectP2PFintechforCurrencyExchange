"""DisputeService — lógica de negocio para disputas"""
from app.core.database import db
from app.core.exceptions import (
    AppException, NotFoundError, AuthorizationError, ConflictError
)
from app.models import Transaction
from app.models.dispute import Dispute
from app.models.user import User
from app.repositories.dispute_repository import DisputeRepository


class DisputeService:
    """
    Orquesta las operaciones de disputas.
    No escribe queries directos; usa DisputeRepository.
    """

    # ─────────────────────────── Acciones de usuario ──────────────────────

    @staticmethod
    def open_dispute(user_id: str, transaction_id: str,
                     reason: str, description: str | None = None) -> Dispute:
        """
        El usuario (comprador o vendedor) abre una disputa sobre su transacción.
        Reglas:
          - La transacción debe existir y el usuario debe ser parte de ella.
          - No se puede disputar una TX completada.
          - Solo puede haber una disputa abierta por transacción.
        """
        txn: Transaction | None = db.session.get(Transaction, transaction_id)
        if not txn:
            raise NotFoundError('Transaction not found')

        if txn.buyer_id != user_id and txn.vendor_id != user_id:
            raise AuthorizationError('Not your transaction')

        if txn.status == 'completed':
            raise AppException('INVALID_STATE',
                               'Cannot dispute a completed transaction', 400)

        if txn.status == 'cancelled':
            raise AppException('INVALID_STATE',
                               'Cannot dispute a cancelled transaction', 400)

        existing = DisputeRepository.get_by_transaction(transaction_id)
        if existing:
            raise ConflictError('There is already an open dispute for this transaction')

        if reason not in Dispute.VALID_REASONS:
            raise AppException('INVALID_REASON',
                               f'Reason must be one of: {", ".join(Dispute.VALID_REASONS)}', 400)

        dispute = DisputeRepository.create(
            transaction_id=transaction_id,
            initiator_id=user_id,
            reason=reason,
            description=description,
        )
        txn.status = 'disputed'
        db.session.commit()
        return dispute

    @staticmethod
    def get_my_disputes(user_id: str, page: int = 1, per_page: int = 20):
        """Devuelve todas las disputas donde el usuario es parte."""
        return DisputeRepository.get_by_user(user_id, page, per_page)

    @staticmethod
    def get_dispute_detail(user_id: str, dispute_id: str) -> Dispute:
        """
        Retorna el detalle de una disputa si el usuario es parte de ella
        o si es admin.
        """
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

    # ─────────────────────────── Acciones de admin ────────────────────────

    @staticmethod
    def list_disputes_admin(page: int = 1, per_page: int = 20,
                            status: str | None = None):
        """Listado paginado para el panel admin."""
        return DisputeRepository.get_all(page, per_page, status)

    @staticmethod
    def take_dispute(admin_id: str, dispute_id: str) -> Dispute:
        """Admin toma la disputa para revisarla (cambia estado a under_review)."""
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
        """
        Admin resuelve la disputa.
        resolution:
          'favour_buyer'  → TX queda como completed (fondos al comprador)
          'favour_vendor' → TX queda como cancelled  (revertido al vendedor)
        """
        if resolution not in ('favour_buyer', 'favour_vendor'):
            raise AppException('INVALID_RESOLUTION',
                               "resolution must be 'favour_buyer' or 'favour_vendor'", 400)

        dispute = DisputeRepository.get_by_id(dispute_id)
        if not dispute:
            raise NotFoundError('Dispute not found')

        if dispute.status == 'resolved':
            raise ConflictError('Dispute is already resolved')

        if dispute.status == 'closed':
            raise AppException('INVALID_STATE', 'Cannot resolve a closed dispute', 400)

        # Actualizar estado de la transacción según la resolución
        txn: Transaction = dispute.transaction
        if txn:
            txn.status = 'completed' if resolution == 'favour_buyer' else 'cancelled'

            # Actualizar contadores si el resultado favorece al comprador
            if resolution == 'favour_buyer':
                buyer: User | None = db.session.get(User, txn.buyer_id)
                vendor: User | None = db.session.get(User, txn.vendor_id)
                if buyer:
                    buyer.total_transactions = (buyer.total_transactions or 0) + 1
                if vendor:
                    vendor.total_transactions = (vendor.total_transactions or 0) + 1

        DisputeRepository.resolve(dispute, admin_id, resolution, resolution_note)
        db.session.commit()
        return dispute
