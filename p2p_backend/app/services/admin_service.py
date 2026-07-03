from app.core.database import db
from app.core.audit import log_action
from app.core.exceptions import NotFoundError, AppException
from app.models.user import User
from app.repositories.admin_repository import AdminRepository
from app.repositories.user_repository import UserRepository
from app.services.dispute_service import DisputeService
from app.services.complaint_service import ComplaintService
from app.services.user_service import UserService


class AdminService:

    @staticmethod
    def get_dashboard() -> dict:
        return AdminRepository.get_dashboard_stats()

    @staticmethod
    def list_users(page: int = 1, per_page: int = 20,
                   role: str | None = None, active: bool | None = None) -> dict:
        pagination = AdminRepository.get_users_paginated(page, per_page, role, active)
        return {
            'users': [
                {
                    'id': u.id,
                    'email': u.email,
                    'full_name': u.full_name,
                    'phone': u.phone,
                    'role': u.role,
                    'kyc_verified': u.kyc_verified,
                    'rating': u.rating,
                    'total_transactions': u.total_transactions,
                    'is_active': u.is_active,
                    'is_banned': u.is_banned,
                    'created_at': u.created_at.isoformat(),
                }
                for u in pagination.items
            ],
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
    def get_user(user_id: str) -> dict:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')
        total_disputes = AdminRepository.get_user_dispute_count(user_id)
        return {
            **user.to_dict(),
            'is_banned': user.is_banned,
            'created_at': user.created_at.isoformat(),
            'total_disputes': total_disputes,
        }

    @staticmethod
    def ban_user(admin_id: str, user_id: str, banned: bool) -> dict:
        user = UserRepository.get_by_id(user_id)
        if not user:
            raise NotFoundError('User not found')

        user.is_banned = banned
        user.is_active = not banned

        log_action(
            admin_id=admin_id,
            action='ban_user' if banned else 'unban_user',
            resource=f'user:{user_id}',
            changes={'banned': banned},
        )

        db.session.commit()
        action = 'banned' if user.is_banned else 'unbanned'
        return {'message': f'User {action}', 'user_id': user_id, 'is_banned': user.is_banned}

    @staticmethod
    def review_user_kyc(user_id: str, approved: bool, note: str | None = None) -> dict:
        user = UserService.review_kyc(user_id, approved, note)
        return {
            'message': 'KYC approved' if approved else 'KYC rejected',
            'user_id': user.id,
            'kyc_status': user.kyc_status,
            'kyc_verified': user.kyc_verified,
        }

    @staticmethod
    def list_disputes(page: int = 1, per_page: int = 20,
                      status: str | None = None) -> dict:
        pagination = DisputeService.list_disputes_admin(page, per_page, status)
        return {
            'disputes': [d.to_dict(include_transaction=True) for d in pagination.items],
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
    def get_dispute(admin_id: str, dispute_id: str):
        return DisputeService.get_dispute_detail(admin_id, dispute_id)

    @staticmethod
    def take_dispute(admin_id: str, dispute_id: str) -> dict:
        dispute = DisputeService.take_dispute(admin_id, dispute_id)
        db.session.commit()

        return {
            'message': 'Dispute is now under review',
            'dispute_id': dispute.id,
            'status': dispute.status,
            'reviewed_by': admin_id,
        }

    @staticmethod
    def resolve_dispute(admin_id: str, dispute_id: str,
                        resolution: str, resolution_note: str | None = None) -> dict:
        dispute = DisputeService.resolve_dispute(
            admin_id=admin_id,
            dispute_id=dispute_id,
            resolution=resolution,
            resolution_note=resolution_note,
        )

        log_action(
            admin_id=admin_id,
            action='resolve_dispute',
            resource=f'dispute:{dispute_id}',
            changes={'resolution': resolution, 'resolution_note': resolution_note},
        )
        db.session.commit()

        return {
            'message': 'Dispute resolved',
            'dispute_id': dispute.id,
            'status': dispute.status,
            'resolution': dispute.resolution,
            'resolution_note': dispute.resolution_note,
            'resolved_at': dispute.resolved_at.isoformat() if dispute.resolved_at else None,
            'transaction_status': dispute.transaction.status if dispute.transaction else None,
        }

    @staticmethod
    def list_complaints(page: int = 1, per_page: int = 20,
                        status: str | None = None) -> dict:
        return ComplaintService.list_all(page, per_page, status)

    @staticmethod
    def get_complaint(complaint_id: str) -> dict:
        return ComplaintService.get_by_id(complaint_id)

    @staticmethod
    def resolve_complaint(admin_id: str, complaint_id: str, admin_note: str) -> dict:
        result = ComplaintService.resolve(complaint_id, admin_note)
        log_action(
            admin_id=admin_id,
            action='resolve_complaint',
            resource=f'complaint:{complaint_id}',
            changes={'admin_note': admin_note},
        )
        db.session.commit()
        return result
