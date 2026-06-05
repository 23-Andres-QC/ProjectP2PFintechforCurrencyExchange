"""Notifications routes — /api/v1/notifications/*"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError
from app.models import Notification

notifications_bp = Blueprint('notifications', __name__, url_prefix='/notifications')


def _notif_dict(n: Notification) -> dict:
    return {
        'id': n.id,
        'user_id': n.user_id,
        'type': n.type,
        'title': n.title,
        'body': n.body,
        'is_read': n.is_read,
        'resource_id': n.resource_id,
        'created_at': n.created_at.isoformat(),
        'updated_at': n.updated_at.isoformat(),
    }


# ── GET /notifications  ──────────────────────────────────────────────────────
@notifications_bp.route('', methods=['GET'])
@notifications_bp.route('/', methods=['GET'])
@jwt_required()
def list_notifications():
    user_id = get_jwt_identity()
    only_unread = request.args.get('unread', '').lower() == 'true'

    query = Notification.query.filter_by(user_id=user_id)
    if only_unread:
        query = query.filter_by(is_read=False)

    notifications = query.order_by(Notification.created_at.desc()).all()
    unread_count = Notification.query.filter_by(user_id=user_id, is_read=False).count()

    return {
        'notifications': [_notif_dict(n) for n in notifications],
        'unread_count': unread_count,
    }, 200


# ── GET /notifications/unread-count  ─────────────────────────────────────────
@notifications_bp.route('/unread-count', methods=['GET'])
@jwt_required()
def unread_count():
    user_id = get_jwt_identity()
    count = Notification.query.filter_by(user_id=user_id, is_read=False).count()
    return {'unread_count': count}, 200


# ── GET /notifications/<id>  ─────────────────────────────────────────────────
@notifications_bp.route('/<notif_id>', methods=['GET'])
@jwt_required()
def get_notification(notif_id):
    user_id = get_jwt_identity()
    notif = db.session.get(Notification, notif_id)
    if not notif:
        raise NotFoundError('Notification not found')
    if notif.user_id != user_id:
        raise AuthorizationError('Not your notification')
    return _notif_dict(notif), 200


# ── PATCH /notifications/<id>/read  ──────────────────────────────────────────
@notifications_bp.route('/<notif_id>/read', methods=['PATCH'])
@jwt_required()
def mark_read(notif_id):
    user_id = get_jwt_identity()
    notif = db.session.get(Notification, notif_id)
    if not notif:
        raise NotFoundError('Notification not found')
    if notif.user_id != user_id:
        raise AuthorizationError('Not your notification')
    notif.is_read = True
    db.session.commit()
    return _notif_dict(notif), 200


# ── POST /notifications/mark-all-read  ───────────────────────────────────────
@notifications_bp.route('/mark-all-read', methods=['POST'])
@jwt_required()
def mark_all_read():
    user_id = get_jwt_identity()
    updated = Notification.query.filter_by(user_id=user_id, is_read=False).update({'is_read': True})
    db.session.commit()
    return {'marked_read': updated}, 200


# ── DELETE /notifications/<id>  ───────────────────────────────────────────────
@notifications_bp.route('/<notif_id>', methods=['DELETE'])
@jwt_required()
def delete_notification(notif_id):
    user_id = get_jwt_identity()
    notif = db.session.get(Notification, notif_id)
    if not notif:
        raise NotFoundError('Notification not found')
    if notif.user_id != user_id:
        raise AuthorizationError('Not your notification')
    db.session.delete(notif)
    db.session.commit()
    return {'message': 'Notification deleted'}, 200


# ── DELETE /notifications  (borrar todas del usuario)  ───────────────────────
@notifications_bp.route('', methods=['DELETE'])
@notifications_bp.route('/', methods=['DELETE'])
@jwt_required()
def delete_all_notifications():
    user_id = get_jwt_identity()
    deleted = Notification.query.filter_by(user_id=user_id).delete()
    db.session.commit()
    return {'deleted': deleted}, 200
