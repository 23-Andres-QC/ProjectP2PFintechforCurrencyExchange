from app.core.database import db
from app.models import Notification


def notify(user_id: str, type: str, title: str, body: str, resource_id: str = None):
    notif = Notification(
        user_id=user_id,
        type=type,
        title=title,
        body=body,
        resource_id=resource_id,
    )
    db.session.add(notif)
    return notif
