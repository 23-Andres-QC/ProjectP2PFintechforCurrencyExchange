from app.core.database import db
from app.core.exceptions import NotFoundError, AppException
from app.core.notifications import notify
from app.models.complaint import Complaint
from app.models.user import User
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
        db.session.flush()
        ComplaintService._notify_admins_new_complaint(complaint)
        db.session.commit()
        return complaint.to_dict()

    @staticmethod
    def _notify_admins_new_complaint(complaint: Complaint) -> None:
        admins = User.query.filter_by(
            role='admin',
            is_active=True,
            is_banned=False,
        ).all()
        for admin in admins:
            notify(
                user_id=admin.id,
                type='admin',
                title='Nuevo reclamo recibido',
                body=(
                    f'Un usuario registro un reclamo ({complaint.type}): '
                    f'{complaint.description[:120]}'
                ),
                resource_id=complaint.id,
            )

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

        notify(
            user_id=complaint.user_id,
            type='complaint',
            title='Tu reclamo fue resuelto',
            body=(
                'Un administrador respondio tu reclamo. '
                f'Respuesta: {admin_note.strip()}'
            ),
            resource_id=complaint.id,
        )

        db.session.commit()
        return complaint.to_dict()
