from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.notification_service import NotificationService
from app.core.database import db
from app.models.user import User

notifications_bp = Blueprint('notifications', __name__, url_prefix='/notifications')


@notifications_bp.route('/fcm-token', methods=['POST'])
@jwt_required()
def register_fcm_token():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    token = data.get('fcm_token', '').strip()
    if not token:
        return {'error': 'fcm_token is required'}, 400
    user = db.session.get(User, user_id)
    if not user:
        return {'error': 'User not found'}, 404
    user.fcm_token = token
    db.session.commit()
    return {'message': 'FCM token registered'}, 200


@notifications_bp.route('', methods=['GET'])
@notifications_bp.route('/', methods=['GET'])
@jwt_required()
def list_notifications():
    user_id = get_jwt_identity()
    only_unread = request.args.get('unread', '').lower() == 'true'
    return NotificationService.list_for_user(user_id, only_unread), 200


@notifications_bp.route('/unread-count', methods=['GET'])
@jwt_required()
def unread_count():
    user_id = get_jwt_identity()
    return {'unread_count': NotificationService.unread_count(user_id)}, 200


@notifications_bp.route('/<notif_id>', methods=['GET'])
@jwt_required()
def get_notification(notif_id):
    user_id = get_jwt_identity()
    return NotificationService.get(user_id, notif_id), 200


@notifications_bp.route('/<notif_id>/read', methods=['PATCH'])
@jwt_required()
def mark_read(notif_id):
    user_id = get_jwt_identity()
    return NotificationService.mark_read(user_id, notif_id), 200


@notifications_bp.route('/mark-all-read', methods=['POST'])
@jwt_required()
def mark_all_read():
    user_id = get_jwt_identity()
    return NotificationService.mark_all_read(user_id), 200


@notifications_bp.route('/<notif_id>', methods=['DELETE'])
@jwt_required()
def delete_notification(notif_id):
    user_id = get_jwt_identity()
    return NotificationService.delete(user_id, notif_id), 200


@notifications_bp.route('', methods=['DELETE'])
@notifications_bp.route('/', methods=['DELETE'])
@jwt_required()
def delete_all_notifications():
    user_id = get_jwt_identity()
    return NotificationService.delete_all(user_id), 200
