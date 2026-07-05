from flask_jwt_extended import JWTManager
from werkzeug.security import generate_password_hash, check_password_hash
from app.core.database import db

jwt = JWTManager()


@jwt.token_in_blocklist_loader
def is_token_revoked(jwt_header, jwt_payload):
    user_id = jwt_payload.get('sub')
    if not user_id:
        return True
    from app.models.user import User
    user = db.session.get(User, user_id)
    return not user or not user.is_active or user.is_banned

def hash_password(password: str) -> str:
    return generate_password_hash(password, method='pbkdf2:sha256')

def verify_password(password_hash: str, password: str) -> bool:
    return check_password_hash(password_hash, password)
