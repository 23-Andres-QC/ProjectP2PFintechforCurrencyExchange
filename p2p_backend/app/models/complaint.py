"""Modelo Complaint — reclamos de usuarios"""
import uuid
from datetime import datetime
from app.core.database import db


class Complaint(db.Model):
    __tablename__ = 'complaints'

    id          = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id     = db.Column(db.String(36), db.ForeignKey('users.id'), nullable=False, index=True)
    type        = db.Column(db.String(50), nullable=False)
    description = db.Column(db.Text, nullable=False)
    status      = db.Column(db.String(20), nullable=False, default='pending')
    admin_note  = db.Column(db.Text, nullable=True)
    created_at  = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    updated_at  = db.Column(db.DateTime, nullable=False, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = db.relationship('User', backref='complaints')

    VALID_TYPES = [
        'transaction_issue',
        'platform_error',
        'payment_issue',
        'account_issue',
        'other'
    ]

    VALID_STATUSES = ['pending', 'under_review', 'resolved', 'closed']

    def to_dict(self):
        return {
            'id':          self.id,
            'user_id':     self.user_id,
            'type':        self.type,
            'description': self.description,
            'status':      self.status,
            'admin_note':  self.admin_note,
            'created_at':  self.created_at.isoformat(),
            'updated_at':  self.updated_at.isoformat(),
        }