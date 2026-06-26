from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.transaction_service import TransactionService

transactions_bp = Blueprint('transactions', __name__, url_prefix='/transactions')


@transactions_bp.route('', methods=['GET'])
@transactions_bp.route('/', methods=['GET'])
@jwt_required()
def list_transactions():
    user_id = get_jwt_identity()
    status_filter = request.args.get('status')
    return {'transactions': TransactionService.list_for_user(user_id, status_filter)}, 200


@transactions_bp.route('/pending', methods=['GET'])
@jwt_required()
def pending_transactions():
    user_id = get_jwt_identity()
    return {'transactions': TransactionService.pending_for_vendor(user_id)}, 200


@transactions_bp.route('/<txn_id>', methods=['GET'])
@jwt_required()
def get_transaction(txn_id):
    user_id = get_jwt_identity()
    return TransactionService.get(user_id, txn_id), 200


@transactions_bp.route('', methods=['POST'])
@transactions_bp.route('/', methods=['POST'])
@jwt_required()
def create_transaction():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return TransactionService.create(user_id, data), 201


@transactions_bp.route('/<txn_id>/voucher', methods=['POST'])
@jwt_required()
def upload_voucher(txn_id):
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return TransactionService.upload_voucher(user_id, txn_id, data), 201

@transactions_bp.route('/<txn_id>/vendor-voucher', methods=['POST'])
@jwt_required()
def upload_vendor_voucher(txn_id):
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return TransactionService.upload_vendor_voucher(user_id, txn_id, data), 200

@transactions_bp.route('/<txn_id>/confirm', methods=['POST'])
@jwt_required()
def confirm_transaction(txn_id):
    user_id = get_jwt_identity()
    return TransactionService.confirm(user_id, txn_id), 200


@transactions_bp.route('/<txn_id>/dispute', methods=['POST'])
@jwt_required()
def open_dispute(txn_id):
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return TransactionService.open_dispute(user_id, txn_id, data), 201


@transactions_bp.route('/<txn_id>/status', methods=['PATCH'])
@jwt_required()
def update_status(txn_id):
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return TransactionService.update_status(user_id, txn_id, data.get('status')), 200


@transactions_bp.route('/disputes', methods=['GET'])
@jwt_required()
def list_disputes():
    user_id = get_jwt_identity()
    return {'disputes': TransactionService.list_disputes(user_id)}, 200
