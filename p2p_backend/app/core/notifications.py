"""Helper para crear notificaciones desde cualquier parte del sistema."""
from app.core.database import db
from app.models import Notification


def notify(user_id: str, type: str, title: str, body: str, resource_id: str = None):
    """Inserta una notificación para el usuario indicado.

    Llamar dentro de un contexto de request Flask (db.session activo).
    No hace commit — el caller es responsable de hacer commit junto con el
    resto de los cambios de su operación.
    """
    notif = Notification(
        user_id=user_id,
        type=type,
        title=title,
        body=body,
        resource_id=resource_id,
    )
    db.session.add(notif)
    return notif
