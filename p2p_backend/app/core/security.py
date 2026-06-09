from flask_jwt_extended import JWTManager
from werkzeug.security import generate_password_hash, check_password_hash

jwt = JWTManager()

def hash_password(password: str) -> str:
    return generate_password_hash(password, method='pbkdf2:sha256')

def verify_password(password_hash: str, password: str) -> bool:
    return check_password_hash(password_hash, password)
