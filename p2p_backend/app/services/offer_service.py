import json
from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError, AppException
from app.models import Offer
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

        amount = data.get('amount', 0)
        price_per_unit = data.get('price_per_unit', 0)
        if not amount or amount <= 0:
            raise AppException('INVALID_AMOUNT', 'El monto debe ser mayor a 0', 400)
        if not price_per_unit or price_per_unit <= 0:
            raise AppException('INVALID_PRICE', 'El precio debe ser mayor a 0', 400)

        offer = OfferRepository.create(
            vendor_id=user_id,
            from_currency=data.get('currency', 'USD'),
            to_currency=data.get('fiat_currency', 'PEN'),
            amount=amount,
            price_per_unit=price_per_unit,
            offer_type=data.get('offer_type', 'sell'),
            min_transaction=data.get('min_transaction', 0),
            max_transaction=data.get('max_transaction'),
            payment_methods=data.get('payment_methods', []),
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
            if k in ('price_per_unit', 'status', 'available_amount',
                     'min_transaction', 'max_transaction')
        }
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
