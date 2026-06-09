from app.core.database import db
from app.core.exceptions import NotFoundError, AuthorizationError
from app.models import Notification
from app.repositories.notification_repository import NotificationRepository


class NotificationService:

    @staticmethod
    def list_for_user(user_id: str, only_unread: bool = False) -> dict:
        notifications = NotificationRepository.get_by_user(user_id, only_unread)
        unread_count = NotificationRepository.count_unread(user_id)
        return {
            'notifications': [NotificationService._to_dict(n) for n in notifications],
            'unread_count': unread_count,
        }

    @staticmethod
    def unread_count(user_id: str) -> int:
        return NotificationRepository.count_unread(user_id)

    @staticmethod
    def get(user_id: str, notif_id: str) -> dict:
        notif = NotificationRepository.get_by_id(notif_id)
        if not notif:
            raise NotFoundError('Notification not found')
        if notif.user_id != user_id:
            raise AuthorizationError('Not your notification')
        return NotificationService._to_dict(notif)

    @staticmethod
    def mark_read(user_id: str, notif_id: str) -> dict:
        notif = NotificationRepository.get_by_id(notif_id)
        if not notif:
            raise NotFoundError('Notification not found')
        if notif.user_id != user_id:
            raise AuthorizationError('Not your notification')
        NotificationRepository.mark_read(notif)
        db.session.commit()
        return NotificationService._to_dict(notif)

    @staticmethod
    def mark_all_read(user_id: str) -> dict:
        updated = NotificationRepository.mark_all_read(user_id)
        db.session.commit()
        return {'marked_read': updated}

    @staticmethod
    def delete(user_id: str, notif_id: str) -> dict:
        notif = NotificationRepository.get_by_id(notif_id)
        if not notif:
            raise NotFoundError('Notification not found')
        if notif.user_id != user_id:
            raise AuthorizationError('Not your notification')
        NotificationRepository.delete(notif)
        db.session.commit()
        return {'message': 'Notification deleted'}

    @staticmethod
    def delete_all(user_id: str) -> dict:
        deleted = NotificationRepository.delete_all(user_id)
        db.session.commit()
        return {'deleted': deleted}

    @staticmethod
    def _to_dict(n: Notification) -> dict:
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
