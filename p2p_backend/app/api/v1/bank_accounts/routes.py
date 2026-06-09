from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.bank_account_service import BankAccountService
from .schemas import CreateBankAccountSchema

bank_accounts_bp = Blueprint('bank_accounts', __name__, url_prefix='/bank-accounts')


@bank_accounts_bp.route('', methods=['GET'])
@bank_accounts_bp.route('/', methods=['GET'])
@jwt_required()
def list_accounts():
    user_id = get_jwt_identity()
    return {'bank_accounts': BankAccountService.list_for_user(user_id)}, 200


@bank_accounts_bp.route('', methods=['POST'])
@bank_accounts_bp.route('/', methods=['POST'])
@jwt_required()
def create_account():
    user_id = get_jwt_identity()
    data = request.get_json() or {}

    schema = CreateBankAccountSchema(context={"bank_name": data.get("bank_name")})
    errors = schema.validate(data)
    if errors:
        return {'error': errors}, 400

    return BankAccountService.create(user_id, data), 201


@bank_accounts_bp.route('/<account_id>', methods=['DELETE'])
@jwt_required()
def delete_account(account_id):
    user_id = get_jwt_identity()
    return BankAccountService.delete(user_id, account_id), 200


@bank_accounts_bp.route('/<account_id>/set-default', methods=['PATCH'])
@jwt_required()
def set_default_account(account_id):
    user_id = get_jwt_identity()
    return BankAccountService.set_default(user_id, account_id), 200
