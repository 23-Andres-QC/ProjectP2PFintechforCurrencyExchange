import json
from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, AppException
from app.models import Offer
from app.repositories.bank_account_repository import BankAccountRepository
from app.repositories.offer_repository import OfferRepository
from app.repositories.user_repository import UserRepository


class OfferService:

    @staticmethod
    def list_active(currency: str | None = None, fiat: str | None = None,
                    offer_type: str | None = None,
                    current_user_id: str | None = None) -> list[dict]:
        offers = OfferRepository.get_active(
            currency=currency, fiat=fiat,
            offer_type=offer_type, exclude_vendor=current_user_id,
        )
        vendors = {
            u.id: u for u in
            UserRepository.get_by_ids([o.vendor_id for o in offers])
        }
        return [
            OfferService._to_dict(o, with_vendor=True, vendor=vendors.get(o.vendor_id))
            for o in offers
        ]

    @staticmethod
    def get_by_id(offer_id: str) -> dict:
        offer = OfferRepository.get_by_id(offer_id)
        if not offer:
            raise NotFoundError('Offer not found')
        return OfferService._to_dict(offer, with_vendor=True)

    @staticmethod
    def create(user_id: str, data: dict) -> dict:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise AuthorizationError('User not found')
        if not user.is_active or user.is_banned:
            raise AuthorizationError('User is not active')
        if not user.kyc_verified:
            raise AuthorizationError('KYC approval is required to publish offers')
        if user.role == 'admin':
            raise AuthorizationError('Admins cannot publish market offers')
        accounts = BankAccountRepository.get_by_user(user_id)
        if not accounts:
            raise AppException(
                'BANK_ACCOUNT_REQUIRED',
                'Debes registrar una cuenta bancaria antes de publicar una oferta',
                400,
            )

        to_currency = data.get('fiat_currency', 'PEN')
        eligible_accounts = [a for a in accounts if a.currency == to_currency]
        if not eligible_accounts:
            raise AppException(
                'BANK_ACCOUNT_REQUIRED',
                f'Debes registrar una cuenta bancaria en {to_currency} para recibir el pago',
                400,
            )

        requested_methods = data.get('payment_methods') or []
        valid_methods = {
            BankAccountRepository.format_label(account): account
            for account in eligible_accounts
        }
        if requested_methods:
            invalid_methods = [m for m in requested_methods if m not in valid_methods]
            if invalid_methods:
                raise AppException(
                    'INVALID_PAYMENT_METHOD',
                    'La cuenta de pago seleccionada no pertenece al usuario o no coincide con la moneda',
                    400,
                )
            payment_methods = requested_methods
        else:
            default_account = next((a for a in eligible_accounts if a.is_primary), eligible_accounts[0])
            payment_methods = [BankAccountRepository.format_label(default_account)]

        amount = float(data.get('amount') or 0)
        price_per_unit = float(data.get('price_per_unit') or 0)
        min_transaction = float(data.get('min_transaction') or 0)
        max_transaction = data.get('max_transaction')
        max_transaction = float(max_transaction) if max_transaction not in (None, '') else None
        if not amount or amount <= 0:
            raise AppException('INVALID_AMOUNT', 'El monto debe ser mayor a 0', 400)
        if not price_per_unit or price_per_unit <= 0:
            raise AppException('INVALID_PRICE', 'El precio debe ser mayor a 0', 400)
        if min_transaction < 0:
            raise AppException('INVALID_MIN_TRANSACTION', 'El monto minimo no puede ser negativo', 400)
        if max_transaction is not None and max_transaction <= 0:
            raise AppException('INVALID_MAX_TRANSACTION', 'El monto maximo debe ser mayor a 0', 400)
        if max_transaction is not None and min_transaction > max_transaction:
            raise AppException('INVALID_LIMITS', 'El monto minimo no puede superar el maximo', 400)
        if max_transaction is not None and max_transaction > amount:
            raise AppException('INVALID_LIMITS', 'El monto maximo no puede superar el monto total', 400)
        if min_transaction > amount:
            raise AppException('INVALID_LIMITS', 'El monto minimo no puede superar el monto total', 400)

        offer = OfferRepository.create(
            vendor_id=user_id,
            from_currency=data.get('currency', 'USD'),
            to_currency=to_currency,
            amount=amount,
            price_per_unit=price_per_unit,
            offer_type=data.get('offer_type', 'sell'),
            min_transaction=min_transaction,
            max_transaction=max_transaction,
            payment_methods=payment_methods,
        )
        db.session.commit()
        return OfferService._to_dict(offer)

    @staticmethod
    def my_offers(user_id: str) -> list[dict]:
        offers = OfferRepository.get_by_vendor(user_id)
        return [OfferService._to_dict(o) for o in offers]

    @staticmethod
    def match(user_id: str, currency: str, fiat_currency: str,
              offer_type: str | None, amount: float) -> dict:
        offer = OfferRepository.find_match(
            currency=currency,
            fiat_currency=fiat_currency,
            offer_type=offer_type,
            amount=amount,
            exclude_vendor=user_id,
        )
        if not offer:
            raise NotFoundError('No matching offer found')
        return OfferService._to_dict(offer, with_vendor=True)

    @staticmethod
    def update(user_id: str, offer_id: str, data: dict) -> dict:
        offer = OfferRepository.get_by_id(offer_id)
        if not offer:
            raise NotFoundError('Offer not found')
        if offer.vendor_id != user_id:
            raise AuthorizationError('Not your offer')

        allowed = {
            k: v for k, v in data.items()
            if k in ('price_per_unit', 'status', 'min_transaction', 'max_transaction')
        }
        if 'status' in allowed and allowed['status'] not in ('active', 'paused', 'closed', 'cancelled'):
            raise AppException('INVALID_STATUS', 'Invalid offer status', 400)
        new_min = float(allowed.get('min_transaction', offer.min_transaction) or 0)
        new_max_raw = allowed.get('max_transaction', offer.max_transaction)
        new_max = float(new_max_raw) if new_max_raw not in (None, '') else None
        if new_min < 0 or new_min > offer.available_amount:
            raise AppException('INVALID_LIMITS', 'Invalid minimum transaction amount', 400)
        if new_max is not None and (new_max <= 0 or new_min > new_max or new_max > offer.available_amount):
            raise AppException('INVALID_LIMITS', 'Invalid maximum transaction amount', 400)
        OfferRepository.update_fields(offer, allowed)
        db.session.commit()
        return OfferService._to_dict(offer)

    @staticmethod
    def delete(user_id: str, offer_id: str) -> dict:
        offer = OfferRepository.get_by_id(offer_id)
        if not offer:
            raise NotFoundError('Offer not found')
        if offer.vendor_id != user_id:
            raise AuthorizationError('Not your offer')

        OfferRepository.close(offer)
        db.session.commit()
        return {'message': 'Offer cancelled'}

    @staticmethod
    def _to_dict(o: Offer, with_vendor: bool = False, vendor=None) -> dict:
        d = {
            'id': o.id,
            'vendor_id': o.vendor_id,
            'currency': o.from_currency,
            'fiat_currency': o.to_currency,
            'amount': o.amount,
            'available_amount': o.available_amount,
            'price_per_unit': o.price_per_unit,
            'offer_type': o.offer_type,
            'status': o.status,
            'min_transaction': o.min_transaction,
            'max_transaction': o.max_transaction,
            'payment_methods': json.loads(o.payment_methods) if o.payment_methods else [],
            'created_at': o.created_at.isoformat(),
        }
        if with_vendor:
            vendor = vendor if vendor is not None else UserRepository.get_by_id(o.vendor_id)
            d['vendor'] = vendor.to_dict() if vendor else None
        return d
