"""Transactions routes — /api/v1/transactions/*"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, AppException
from app.models import Offer, Transaction, Voucher, Dispute
from app.models.user import User
from app.core.notifications import notify

transactions_bp = Blueprint('transactions', __name__, url_prefix='/transactions')


def _txn_dict(t):
    buyer  = db.session.get(User, t.buyer_id)
    vendor = db.session.get(User, t.vendor_id)
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
    }


@transactions_bp.route('', methods=['GET'])
@transactions_bp.route('/', methods=['GET'])
@jwt_required()
def list_transactions():
    user_id = get_jwt_identity()
    status_filter = request.args.get('status')

    query = Transaction.query.filter(
        (Transaction.buyer_id == user_id) | (Transaction.vendor_id == user_id)
    )
    if status_filter:
        query = query.filter_by(status=status_filter)

    txns = query.order_by(Transaction.created_at.desc()).all()
    return {'transactions': [_txn_dict(t) for t in txns]}, 200


@transactions_bp.route('/pending', methods=['GET'])
@jwt_required()
def pending_transactions():
    user_id = get_jwt_identity()
    txns = Transaction.query.filter(
        Transaction.vendor_id == user_id,
        Transaction.status.in_(('pending', 'voucher_uploaded'))
    ).order_by(Transaction.created_at.desc()).all()
    return {'transactions': [_txn_dict(t) for t in txns]}, 200


@transactions_bp.route('/<txn_id>', methods=['GET'])
@jwt_required()
def get_transaction(txn_id):
    user_id = get_jwt_identity()
    txn = db.session.get(Transaction, txn_id)
    if not txn:
        raise NotFoundError('Transaction not found')
    if txn.buyer_id != user_id and txn.vendor_id != user_id:
        raise AuthorizationError('Not your transaction')
    return _txn_dict(txn), 200


@transactions_bp.route('', methods=['POST'])
@transactions_bp.route('/', methods=['POST'])
@jwt_required()
def create_transaction():
    user_id = get_jwt_identity()
    data = request.get_json() or {}

    offer = db.session.get(Offer, data.get('offer_id'))
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
            raise AppException('INVALID_AMOUNT', f'Amount must be between {offer.min_transaction} and {offer.max_transaction}', 400)
        if amount_from > offer.available_amount:
            raise AppException('INVALID_AMOUNT', 'Amount exceeds available amount', 400)

    txn = Transaction(
        offer_id=offer.id,
        buyer_id=user_id,
        vendor_id=offer.vendor_id,
        amount_from=amount_from,
        amount_to=amount_to,
        exchange_rate=offer.price_per_unit,
        status='pending',
        buyer_payment_account=data.get('buyer_payment_account'),
        vendor_payment_account=data.get('vendor_payment_account'),
    )
    db.session.add(txn)

    offer.available_amount -= amount_from
    if offer.available_amount <= 0:
        offer.status = 'closed'

    notify(
        user_id=txn.buyer_id,
        type='transaction',
        title='Transacción creada',
        body=f'Tu solicitud de cambio está pendiente de confirmación por el vendedor.',
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
    return _txn_dict(txn), 201


@transactions_bp.route('/<txn_id>/voucher', methods=['POST'])
@jwt_required()
def upload_voucher(txn_id):
    user_id = get_jwt_identity()
    txn = db.session.get(Transaction, txn_id)
    if not txn:
        raise NotFoundError('Transaction not found')
    if txn.buyer_id != user_id:
        raise AuthorizationError('Only buyer can upload voucher')

    data = request.get_json() or {}
    voucher = Voucher(
        transaction_id=txn.id,
        sender_id=user_id,
        image_url=data.get('image_url', ''),
        description=data.get('description'),
        status='pending',
    )
    db.session.add(voucher)
    txn.status = 'voucher_uploaded'

    notify(
        user_id=txn.vendor_id,
        type='voucher',
        title='Comprobante de pago subido',
        body='El comprador subió su comprobante. Por favor revísalo y confirma la transacción.',
        resource_id=txn.id,
    )

    db.session.commit()
    return {'id': voucher.id, 'status': 'pending', 'transaction_status': 'voucher_uploaded'}, 201


@transactions_bp.route('/<txn_id>/confirm', methods=['POST'])
@jwt_required()
def confirm_transaction(txn_id):
    user_id = get_jwt_identity()
    txn = db.session.get(Transaction, txn_id)
    if not txn:
        raise NotFoundError('Transaction not found')
    if txn.vendor_id != user_id:
        raise AuthorizationError('Only vendor can confirm')
    if txn.status not in ('voucher_uploaded', 'pending'):
        raise AppException('INVALID_STATE', f'Cannot confirm from {txn.status}', 400)

    txn.status = 'completed'
    vendor = db.session.get(User, txn.vendor_id)
    buyer  = db.session.get(User, txn.buyer_id)
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
    return {'message': 'Transaction completed', 'status': 'completed'}, 200


@transactions_bp.route('/<txn_id>/dispute', methods=['POST'])
@jwt_required()
def open_dispute(txn_id):
    user_id = get_jwt_identity()
    txn = db.session.get(Transaction, txn_id)
    if not txn:
        raise NotFoundError('Transaction not found')
    if txn.buyer_id != user_id and txn.vendor_id != user_id:
        raise AuthorizationError('Not your transaction')
    if txn.status == 'completed':
        raise AppException('INVALID_STATE', 'Cannot dispute completed transaction', 400)

    data = request.get_json() or {}
    dispute = Dispute(
        transaction_id=txn.id,
        initiator_id=user_id,
        reason=data.get('reason', 'No reason provided'),
        description=data.get('description'),
        status='open',
    )
    db.session.add(dispute)
    txn.status = 'disputed'

    other_id = txn.vendor_id if user_id == txn.buyer_id else txn.buyer_id
    notify(
        user_id=other_id,
        type='dispute',
        title='Disputa abierta en tu transacción',
        body=f'Se abrió una disputa por motivo: {dispute.reason}. Un administrador revisará el caso.',
        resource_id=dispute.id,
    )

    db.session.commit()
    return {'id': dispute.id, 'status': 'open', 'transaction_status': 'disputed'}, 201


@transactions_bp.route('/<txn_id>/status', methods=['PATCH'])
@jwt_required()
def update_status(txn_id):
    user_id = get_jwt_identity()
    txn = db.session.get(Transaction, txn_id)
    if not txn:
        raise NotFoundError('Transaction not found')
    if txn.buyer_id != user_id and txn.vendor_id != user_id:
        raise AuthorizationError('Not your transaction')

    data = request.get_json() or {}
    new_status = data.get('status')
    if new_status not in ('cancelled', 'paused'):
        raise AppException('INVALID_STATUS', 'Status must be cancelled or paused', 400)

    txn.status = new_status
    db.session.commit()
    return _txn_dict(txn), 200


@transactions_bp.route('/disputes', methods=['GET'])
@jwt_required()
def list_disputes():
    user_id = get_jwt_identity()
    # Find all disputes where the user is either the buyer or vendor of the transaction
    disputes = db.session.query(Dispute).join(Transaction).filter(
        (Transaction.buyer_id == user_id) | (Transaction.vendor_id == user_id)
    ).all()
    
    result = []
    for d in disputes:
        result.append({
            'id': d.id,
            'transaction_id': d.transaction_id,
            'initiator_id': d.initiator_id,
            'reason': d.reason,
            'description': d.description,
            'status': d.status,
            'created_at': d.created_at.isoformat(),
        })
    return {'disputes': result}, 200
