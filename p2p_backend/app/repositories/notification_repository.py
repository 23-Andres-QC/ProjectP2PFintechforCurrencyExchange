from app.core.database import db
from app.models import Notification


class NotificationRepository:

    @staticmethod
    def get_by_id(notif_id: str) -> Notification | None:
        return db.session.get(Notification, notif_id)

    @staticmethod
    def get_by_user(user_id: str, only_unread: bool = False, limit: int = 200):
        query = Notification.query.filter_by(user_id=user_id)
        if only_unread:
            query = query.filter_by(is_read=False)
        return query.order_by(Notification.created_at.desc()).limit(limit).all()

    @staticmethod
    def count_unread(user_id: str) -> int:
        return Notification.query.filter_by(user_id=user_id, is_read=False).count()

    @staticmethod
    def mark_read(notif: Notification) -> Notification:
        notif.is_read = True
        return notif

    @staticmethod
    def mark_all_read(user_id: str) -> int:
        return Notification.query.filter_by(user_id=user_id, is_read=False).update({'is_read': True})

    @staticmethod
    def delete(notif: Notification):
        db.session.delete(notif)

    @staticmethod
    def delete_all(user_id: str) -> int:
        return Notification.query.filter_by(user_id=user_id).delete()
