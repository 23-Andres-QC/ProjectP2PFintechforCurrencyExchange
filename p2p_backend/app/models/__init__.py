"""Modelos para las tablas"""
from app.core.database import db, BaseModel


class Currency(BaseModel):
    __tablename__ = 'currencies'
    code   = db.Column(db.String(10),  unique=True, nullable=False)
    name   = db.Column(db.String(100), nullable=False)
    symbol = db.Column(db.String(10),  nullable=False)


class ExchangeRate(BaseModel):
    __tablename__ = 'exchange_rates'
    from_currency = db.Column(db.String(10), nullable=False)
    to_currency   = db.Column(db.String(10), nullable=False)
    rate          = db.Column(db.Float, nullable=False)


class BankAccount(BaseModel):
    __tablename__ = 'bank_accounts'
    user_id        = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    bank_name      = db.Column(db.String(100), nullable=False)
    account_number = db.Column(db.String(50),  nullable=False)
    account_holder = db.Column(db.String(255), nullable=False)
    account_type   = db.Column(db.String(20),  nullable=False)
    currency       = db.Column(db.String(10),  nullable=False)
    is_primary     = db.Column(db.Boolean, default=False)
    is_verified    = db.Column(db.Boolean, default=False)


class Offer(BaseModel):
    __tablename__ = 'offers'
    vendor_id      = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    from_currency  = db.Column(db.String(10), nullable=False)   # divisa que ofrece el vendedor
    to_currency    = db.Column(db.String(10), nullable=False)   # divisa que recibe el vendedor
    amount         = db.Column(db.Float, nullable=False)
    available_amount = db.Column(db.Float, nullable=False)
    price_per_unit = db.Column(db.Float, nullable=False)
    offer_type     = db.Column(db.String(20), nullable=False)   # 'full' | 'partial'
    status         = db.Column(db.String(20), default='active', index=True)
    min_transaction = db.Column(db.Float, default=0)
    max_transaction = db.Column(db.Float)
    payment_methods = db.Column(db.Text)


class Transaction(BaseModel):
    __tablename__ = 'transactions'
    offer_id       = db.Column(db.String(36), db.ForeignKey('offers.id'), nullable=False)
    buyer_id       = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    vendor_id      = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    amount_from    = db.Column(db.Float, nullable=False)   # monto en divisa origen
    amount_to      = db.Column(db.Float, nullable=False)   # monto en divisa destino
    exchange_rate  = db.Column(db.Float, nullable=False)
    status         = db.Column(db.String(20), default='pending', index=True)
    buyer_payment_account  = db.Column(db.Text)
    vendor_payment_account = db.Column(db.Text)


class Voucher(BaseModel):
    __tablename__ = 'vouchers'
    transaction_id = db.Column(db.String(36), db.ForeignKey('transactions.id'), nullable=False)
    sender_id      = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False)
    image_url      = db.Column(db.String(500), nullable=False)
    description    = db.Column(db.Text)
    status         = db.Column(db.String(20), default='pending')


class Rating(BaseModel):
    __tablename__ = 'ratings'
    transaction_id = db.Column(db.String(36), db.ForeignKey('transactions.id'), nullable=False)
    rater_id       = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    ratee_id       = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False)
    score          = db.Column(db.Integer, nullable=False)
    comment        = db.Column(db.Text)





class AuditLog(BaseModel):
    __tablename__ = 'audit_logs'
    user_id  = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=True)
    action   = db.Column(db.String(100), nullable=False)
    resource = db.Column(db.String(100), nullable=False)
    changes  = db.Column(db.Text)

from app.models.dispute import Dispute
from app.models.complaint import Complaint


class Notification(BaseModel):
    __tablename__ = 'notifications'
    user_id     = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    type        = db.Column(db.String(50), nullable=False)   # login | transaction | voucher | dispute | offer | security | admin
    title       = db.Column(db.String(255), nullable=False)
    body        = db.Column(db.Text, nullable=False)
    is_read     = db.Column(db.Boolean, default=False, nullable=False)
    resource_id = db.Column(db.String(36), nullable=True)    # id del recurso relacionado
