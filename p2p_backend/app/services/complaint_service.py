from app.core.database import db
from app.core.exceptions import NotFoundError, AppException
from app.models.complaint import Complaint
from app.repositories.complaint_repository import ComplaintRepository


class ComplaintService:

    @staticmethod
    def create(user_id: str, complaint_type: str, description: str) -> dict:
        if not complaint_type or complaint_type not in Complaint.VALID_TYPES:
            raise AppException(
                'INVALID_TYPE',
                f'Type must be one of: {", ".join(Complaint.VALID_TYPES)}',
                400,
            )
        if not description or not description.strip():
            raise AppException('MISSING_FIELD', 'description is required', 400)

        complaint = ComplaintRepository.create(
            user_id=user_id,
            complaint_type=complaint_type,
            description=description.strip(),
        )
        db.session.commit()
        return complaint.to_dict()

    @staticmethod
    def my_complaints(user_id: str, page: int = 1, per_page: int = 20) -> dict:
        pagination = ComplaintRepository.get_by_user_paginated(user_id, page, per_page)
        return {
            'complaints': [c.to_dict() for c in pagination.items],
            'pagination': {
                'page': pagination.page,
                'per_page': pagination.per_page,
                'total': pagination.total,
                'pages': pagination.pages,
                'has_next': pagination.has_next,
                'has_prev': pagination.has_prev,
            },
        }

    @staticmethod
    def list_all(page: int = 1, per_page: int = 20,
                 status: str | None = None) -> dict:
        pagination = ComplaintRepository.get_all_paginated(page, per_page, status)
        return {
            'complaints': [c.to_dict() for c in pagination.items],
            'pagination': {
                'page': pagination.page,
                'per_page': pagination.per_page,
                'total': pagination.total,
                'pages': pagination.pages,
                'has_next': pagination.has_next,
                'has_prev': pagination.has_prev,
            },
        }

    @staticmethod
    def get_by_id(complaint_id: str) -> dict:
        complaint = ComplaintRepository.get_by_id(complaint_id)
        if not complaint:
            raise NotFoundError('Complaint not found')
        return complaint.to_dict()

    @staticmethod
    def resolve(complaint_id: str, admin_note: str) -> dict:
        complaint = ComplaintRepository.get_by_id(complaint_id)
        if not complaint:
            raise NotFoundError('Complaint not found')

        if complaint.status in ('resolved', 'closed'):
            raise AppException('INVALID_STATE', 'Complaint is already resolved', 400)

        if not admin_note or not admin_note.strip():
            raise AppException('MISSING_FIELD', 'admin_note is required', 400)

        ComplaintRepository.resolve(complaint, admin_note.strip())

        db.session.commit()
        return complaint.to_dict()
