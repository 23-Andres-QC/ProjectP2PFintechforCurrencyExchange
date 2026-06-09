from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.rating_service import RatingService

ratings_bp = Blueprint('ratings', __name__, url_prefix='/ratings')


@ratings_bp.route('/received', methods=['GET'])
@jwt_required()
def get_received_ratings():
    user_id = get_jwt_identity()
    return RatingService.get_received(user_id), 200


@ratings_bp.route('', methods=['POST'])
@ratings_bp.route('/', methods=['POST'])
@jwt_required()
def create_rating():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    return RatingService.create(user_id, data), 201
