from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, ConflictError
from app.models import BankAccount
from app.repositories.bank_account_repository import BankAccountRepository


class BankAccountService:

    @staticmethod
    def list_for_user(user_id: str) -> list[dict]:
        accounts = BankAccountRepository.get_by_user(user_id)
        return [BankAccountService._to_dict(a) for a in accounts]

    @staticmethod
    def create(user_id: str, data: dict) -> dict:
        bank_name = (data.get('bank_name') or '').strip()
        account_number = (data.get('account_number') or '').strip()
        account_holder = (data.get('account_holder') or '').strip()
        currency = (data.get('currency') or 'PEN').strip().upper()

        existing = BankAccountRepository.find_duplicate(
            user_id=user_id,
            account_number=account_number,
        )
        if existing:
            raise ConflictError('Ya tienes una cuenta con ese número registrada')

        is_first_account = not BankAccountRepository.get_by_user(user_id)
        is_primary = bool(data.get('is_primary')) or is_first_account

        if is_primary:
            BankAccountRepository.clear_primary(user_id)

        account = BankAccountRepository.create(
            user_id=user_id,
            bank_name=bank_name,
            account_number=account_number,
            account_holder=account_holder,
            account_type=data.get('account_type', 'savings'),
            currency=currency,
            is_primary=is_primary,
        )
        db.session.commit()
        return BankAccountService._to_dict(account)

    @staticmethod
    def delete(user_id: str, account_id: str) -> dict:
        account = BankAccountRepository.get_by_id(account_id)
        if not account:
            raise NotFoundError('Account not found')
        if account.user_id != user_id:
            raise AuthorizationError('Not your account')

        if BankAccountRepository.has_active_transaction(account):
            raise ConflictError('No puedes eliminar esta cuenta porque tiene una transacción activa')

        BankAccountRepository.delete(account)
        db.session.commit()
        return {'message': 'Cuenta eliminada correctamente'}

    @staticmethod
    def set_default(user_id: str, account_id: str) -> dict:
        account = BankAccountRepository.get_by_id(account_id)
        if not account:
            raise NotFoundError('Account not found')
        if account.user_id != user_id:
            raise AuthorizationError('Not your account')

        BankAccountRepository.clear_primary(user_id)
        BankAccountRepository.set_primary(account)
        db.session.commit()
        return {
            'message': 'Cuenta marcada como principal',
            'data': BankAccountService._to_dict(account),
        }

    @staticmethod
    def _to_dict(a: BankAccount) -> dict:
        return {
            'id': a.id,
            'user_id': a.user_id,
            'bank_name': a.bank_name,
            'account_number': a.account_number,
            'account_holder': a.account_holder,
            'account_type': a.account_type,
            'currency': a.currency,
            'is_primary': a.is_primary,
            'is_verified': a.is_verified,
            'created_at': a.created_at.isoformat(),
        }
