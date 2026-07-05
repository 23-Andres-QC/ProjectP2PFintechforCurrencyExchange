from app.core.database import db
from app.models import BankAccount, Transaction


class BankAccountRepository:

    @staticmethod
    def format_label(account: BankAccount) -> str:
        return f"{account.bank_name} \u00b7 {account.account_number}"

    @staticmethod
    def get_by_id(account_id: str) -> BankAccount | None:
        return db.session.get(BankAccount, account_id)

    @staticmethod
    def get_by_user(user_id: str):
        return BankAccount.query.filter_by(user_id=user_id).all()

    @staticmethod
    def find_duplicate(user_id: str, account_number: str) -> BankAccount | None:
        return BankAccount.query.filter_by(
            user_id=user_id,
            account_number=account_number
        ).first()

    @staticmethod
    def clear_primary(user_id: str):
        BankAccount.query.filter_by(
            user_id=user_id,
            is_primary=True
        ).update({'is_primary': False})

    @staticmethod
    def create(user_id: str, bank_name: str, account_number: str,
               account_holder: str, account_type: str, currency: str,
               is_primary: bool = False) -> BankAccount:
        account = BankAccount(
            user_id=user_id,
            bank_name=bank_name,
            account_number=account_number,
            account_holder=account_holder,
            account_type=account_type,
            currency=currency,
            is_primary=is_primary,
        )
        db.session.add(account)
        return account

    @staticmethod
    def has_active_transaction(account: BankAccount) -> bool:
        label = BankAccountRepository.format_label(account)
        return Transaction.query.filter(
            (Transaction.buyer_payment_account == label) |
            (Transaction.vendor_payment_account == label),
            Transaction.status.in_([
                'pending', 'accepted', 'voucher_uploaded', 'disputed'
            ])
        ).first() is not None

    @staticmethod
    def delete(account: BankAccount):
        db.session.delete(account)

    @staticmethod
    def set_primary(account: BankAccount) -> BankAccount:
        account.is_primary = True
        return account
