from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.offer_service import OfferService

offers_bp = Blueprint('offers', __name__, url_prefix='/offers')


@offers_bp.route('', methods=['GET'])
@offers_bp.route('/', methods=['GET'])
@jwt_required(optional=True)
def list_offers():
    current_user_id = get_jwt_identity()
    offers = OfferService.list_active(
        currency=request.args.get('currency'),
        fiat=request.args.get('fiat_currency'),
        offer_type=request.args.get('type'),
        current_user_id=current_user_id,
    )
    return {'offers': offers}, 200


@offers_bp.route('/<offer_id>', methods=['GET'])
def get_offer(offer_id):
    return OfferService.get_by_id(offer_id), 200


@offers_bp.route('', methods=['POST'])
@offers_bp.route('/', methods=['POST'])
@jwt_required()
def create_offer():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return OfferService.create(user_id, data), 201


@offers_bp.route('/my-offers', methods=['GET'])
@jwt_required()
def my_offers():
    user_id = get_jwt_identity()
    return {'offers': OfferService.my_offers(user_id)}, 200


@offers_bp.route('/match', methods=['POST'])
@jwt_required()
def match_offer():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return OfferService.match(
        user_id=user_id,
        currency=data.get('currency', 'USD'),
        fiat_currency=data.get('fiat_currency', 'PEN'),
        offer_type=data.get('offer_type'),
        amount=data.get('amount', 0),
    ), 200


@offers_bp.route('/<offer_id>', methods=['PATCH'])
@jwt_required()
def update_offer(offer_id):
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return OfferService.update(user_id, offer_id, data), 200


@offers_bp.route('/<offer_id>', methods=['DELETE'])
@jwt_required()
def delete_offer(offer_id):
    user_id = get_jwt_identity()
    return OfferService.delete(user_id, offer_id), 200
