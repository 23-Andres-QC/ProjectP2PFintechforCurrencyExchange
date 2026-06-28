import threading

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

    # Enviar push notification si el usuario tiene token FCM registrado.
    # Se hace en un hilo aparte porque el SDK de Firebase hace una llamada de red
    # síncrona: si se llama inline, el request HTTP completo (ej. confirmar una
    # transacción) se queda esperando esa respuesta antes de responderle a la app.
    try:
        from app.models.user import User
        user = db.session.get(User, user_id)
        if user and user.fcm_token:
            fcm_token = user.fcm_token
            threading.Thread(
                target=_send_push_safe,
                args=(fcm_token, title, body, {'type': type, 'resource_id': resource_id or ''}),
                daemon=True,
            ).start()
    except Exception:
        pass  # Push falla silenciosamente, la notificación interna siempre se guarda

    return notif


def _send_push_safe(fcm_token: str, title: str, body: str, data: dict):
    try:
        from app.core.push import send_push
        send_push(fcm_token=fcm_token, title=title, body=body, data=data)
    except Exception:
        pass
