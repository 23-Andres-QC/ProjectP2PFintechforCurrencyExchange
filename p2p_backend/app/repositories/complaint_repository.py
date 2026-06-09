from app.core.database import db
from app.models.complaint import Complaint


class ComplaintRepository:

    @staticmethod
    def get_by_id(complaint_id: str) -> Complaint | None:
        return db.session.get(Complaint, complaint_id)

    @staticmethod
    def get_by_user_paginated(user_id: str, page: int = 1, per_page: int = 20):
        return Complaint.query.filter_by(user_id=user_id)\
            .order_by(Complaint.created_at.desc())\
            .paginate(page=page, per_page=per_page, error_out=False)

    @staticmethod
    def get_all_paginated(page: int = 1, per_page: int = 20,
                          status: str | None = None):
        query = Complaint.query
        if status:
            query = query.filter_by(status=status)
        return query.order_by(Complaint.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

    @staticmethod
    def create(user_id: str, complaint_type: str, description: str) -> Complaint:
        complaint = Complaint(
            user_id=user_id,
            type=complaint_type,
            description=description,
        )
        db.session.add(complaint)
        return complaint

    @staticmethod
    def resolve(complaint: Complaint, admin_note: str) -> Complaint:
        complaint.status = 'resolved'
        complaint.admin_note = admin_note
        return complaint
