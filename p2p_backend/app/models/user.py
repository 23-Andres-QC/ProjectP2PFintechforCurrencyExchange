from app.core.database import db, BaseModel
from app.core.security import hash_password, verify_password

class User(BaseModel):
    __tablename__ = 'users'

    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(255), nullable=False)
    full_name = db.Column(db.String(255), nullable=False)
    dni = db.Column(db.String(20), unique=True, nullable=True)
    phone = db.Column(db.String(20), nullable=True)
    avatar_url = db.Column(db.String(500), nullable=True)
    signature_url = db.Column(db.String(500), nullable=True)  # ← AGREGÉ
    dni_image_url = db.Column(db.String(500), nullable=True)# ← AGREGÉ
    dni_back_url = db.Column(db.String(500), nullable=True)
    selfie_url = db.Column(db.String(500), nullable=True)
    
    role = db.Column(db.String(20), default='buyer')
    kyc_verified = db.Column(db.Boolean, default=False)
    rating = db.Column(db.Float, default=0.0)
    total_transactions = db.Column(db.Integer, default=0)
    is_active = db.Column(db.Boolean, default=True)
    is_banned = db.Column(db.Boolean, default=False)
    fcm_token = db.Column(db.String(500), nullable=True)

    def set_password(self, password: str):
        self.password_hash = hash_password(password)

    def check_password(self, password: str) -> bool:
        return verify_password(self.password_hash, password)

    def completed_transactions_count(self) -> int:
        from app.models import Transaction

        return Transaction.query.filter(
            db.or_(Transaction.buyer_id == self.id, Transaction.vendor_id == self.id),
            Transaction.status.in_(('completed', 'closed')),
        ).count()

    def to_dict(self):
        return {
            'id': self.id,
            'email': self.email,
            'full_name': self.full_name,
            'phone': self.phone,
            'role': self.role,
            'kyc_verified': self.kyc_verified,
            'rating': self.rating,
            'total_transactions': self.completed_transactions_count(),
            'avatar_url': self.avatar_url,
            'is_active': self.is_active,
            'dni': self.dni,
            'dni_image_url': self.dni_image_url,
            'dni_back_url': self.dni_back_url,
            'selfie_url': self.selfie_url,
            'signature_url': self.signature_url,
        }
