import json
from app.core.database import db
from app.models import Offer


class OfferRepository:

    @staticmethod
    def get_by_id(offer_id: str) -> Offer | None:
        return db.session.get(Offer, offer_id)

    @staticmethod
    def get_by_id_for_update(offer_id: str) -> Offer | None:
        """Bloquea la fila (SELECT ... FOR UPDATE) para evitar oversell por compras concurrentes."""
        return db.session.get(Offer, offer_id, with_for_update=True)

    @staticmethod
    def get_active(currency: str | None = None, fiat: str | None = None,
                   offer_type: str | None = None, exclude_vendor: str | None = None,
                   limit: int = 200):
        query = Offer.query.filter_by(status='active')
        if currency:
            query = query.filter_by(from_currency=currency)
        if fiat:
            query = query.filter_by(to_currency=fiat)
        if offer_type:
            query = query.filter_by(offer_type=offer_type)
        if exclude_vendor:
            query = query.filter(Offer.vendor_id != exclude_vendor)
        return query.order_by(Offer.price_per_unit).limit(limit).all()

    @staticmethod
    def get_by_vendor(vendor_id: str):
        return Offer.query.filter_by(vendor_id=vendor_id).order_by(Offer.created_at.desc()).all()

    @staticmethod
    def find_match(currency: str, fiat_currency: str, offer_type: str | None,
                   amount: float, exclude_vendor: str) -> Offer | None:
        query = Offer.query.filter_by(
            status='active',
            from_currency=currency,
            to_currency=fiat_currency,
        ).filter(Offer.vendor_id != exclude_vendor)
        if offer_type:
            query = query.filter_by(offer_type=offer_type)
        if amount:
            query = query.filter(
                Offer.min_transaction <= amount,
                (Offer.max_transaction == None) | (Offer.max_transaction >= amount)
            )
        return query.order_by(Offer.price_per_unit).first()

    @staticmethod
    def create(vendor_id: str, from_currency: str, to_currency: str,
               amount: float, price_per_unit: float, offer_type: str,
               min_transaction: float = 0, max_transaction: float | None = None,
               payment_methods: list | None = None) -> Offer:
        offer = Offer(
            vendor_id=vendor_id,
            from_currency=from_currency,
            to_currency=to_currency,
            amount=amount,
            available_amount=amount,
            price_per_unit=price_per_unit,
            offer_type=offer_type,
            min_transaction=min_transaction,
            max_transaction=max_transaction,
            payment_methods=json.dumps(payment_methods or []),
        )
        db.session.add(offer)
        return offer

    @staticmethod
    def update_fields(offer: Offer, fields: dict) -> Offer:
        for key, value in fields.items():
            setattr(offer, key, value)
        return offer

    @staticmethod
    def close(offer: Offer) -> Offer:
        offer.status = 'closed'
        return offer
