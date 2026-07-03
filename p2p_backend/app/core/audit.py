import json

from app.core.database import db
from app.models import AuditLog


def log_action(admin_id: str, action: str, resource: str, changes: dict | None = None) -> None:
    db.session.add(AuditLog(
        user_id=admin_id,
        action=action,
        resource=resource,
        changes=json.dumps(changes) if changes else None,
    ))
