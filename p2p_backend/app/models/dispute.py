"""Modelo Dispute — tabla disputes"""
from app.core.database import db, BaseModel


class Dispute(BaseModel):
    """
    Representa una disputa abierta sobre una transacción.

    Estados posibles:
        open       → recién abierta, esperando revisión admin
        under_review → admin la tomó, está revisando
        resolved   → resuelta (liberado al comprador o revertido al vendedor)
        closed     → cerrada sin acción (ej. partes llegaron a acuerdo)
    """
    __tablename__ = 'disputes'

    transaction_id  = db.Column(db.String(36), db.ForeignKey('transactions.id'),
                                nullable=False, index=True)
    initiator_id    = db.Column(db.String(36), db.ForeignKey('users.id'),
                                nullable=False)
    reason          = db.Column(db.String(255), nullable=False)
    description     = db.Column(db.Text)
    status          = db.Column(db.String(20), default='open', index=True)

    # Campos de resolución — se rellenan cuando admin resuelve
    resolved_by     = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=True)
    resolution      = db.Column(db.String(20), nullable=True)   # 'favour_buyer' | 'favour_vendor'
    resolution_note = db.Column(db.Text, nullable=True)
    resolved_at     = db.Column(db.DateTime, nullable=True)

    # Relationships (lazy para evitar N+1 en listados)
    transaction = db.relationship('Transaction', foreign_keys=[transaction_id],
                                  backref=db.backref('disputes', lazy='dynamic'), lazy='joined')
    initiator   = db.relationship('User', foreign_keys=[initiator_id], lazy='joined')
    resolver    = db.relationship('User', foreign_keys=[resolved_by], lazy='select')

    # ── Razones de disputa predefinidas ──────────────────────────────────────
    REASON_PAYMENT_NOT_RECEIVED  = 'payment_not_received'
    REASON_WRONG_AMOUNT          = 'wrong_amount'
    REASON_VOUCHER_FAKE          = 'voucher_fake'
    REASON_NO_RESPONSE           = 'no_response'
    REASON_OTHER                 = 'other'

    VALID_REASONS = {
        REASON_PAYMENT_NOT_RECEIVED,
        REASON_WRONG_AMOUNT,
        REASON_VOUCHER_FAKE,
        REASON_NO_RESPONSE,
        REASON_OTHER,
    }

    def to_dict(self, include_transaction=False):
        data = {
            'id':              self.id,
            'transaction_id':  self.transaction_id,
            'initiator_id':    self.initiator_id,
            'initiator_name':  self.initiator.full_name if self.initiator else None,
            'reason':          self.reason,
            'description':     self.description,
            'status':          self.status,
            'resolved_by':     self.resolved_by,
            'resolution':      self.resolution,
            'resolution_note': self.resolution_note,
            'resolved_at':     self.resolved_at.isoformat() if self.resolved_at else None,
            'created_at':      self.created_at.isoformat(),
            'updated_at':      self.updated_at.isoformat(),
        }
        if include_transaction and self.transaction:
            t = self.transaction
            data['transaction'] = {
                'id':           t.id,
                'amount_from':  t.amount_from,
                'amount_to':    t.amount_to,
                'exchange_rate': t.exchange_rate,
                'status':       t.status,
                'buyer_id':     t.buyer_id,
                'vendor_id':    t.vendor_id,
            }
        return data
