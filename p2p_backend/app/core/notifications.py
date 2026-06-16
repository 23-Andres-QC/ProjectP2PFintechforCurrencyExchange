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

    # Enviar push notification si el usuario tiene token FCM registrado
    try:
        from app.models.user import User
        from app.core.push import send_push
        user = db.session.get(User, user_id)
        if user and user.fcm_token:
            send_push(
                fcm_token=user.fcm_token,
                title=title,
                body=body,
                data={'type': type, 'resource_id': resource_id or ''},
            )
    except Exception:
        pass  # Push falla silenciosamente, la notificación interna siempre se guarda

    return notif
