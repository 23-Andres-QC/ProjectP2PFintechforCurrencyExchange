"""Admin routes — /api/v1/admin/*

Solo accesible por usuarios con role == 'admin'.
"""
from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func

from app.core.database import db
from app.core.exceptions import AuthorizationError, NotFoundError, AppException
from app.models import Transaction, Dispute
from app.models.user import User
from app.services.dispute_service import DisputeService
from app.core.notifications import notify


admin_bp = Blueprint('admin', __name__, url_prefix='/admin')


# ── Guard ─────────────────────────────────────────────────────────────────────

def _require_admin() -> User:
    """Verifica que el usuario autenticado sea admin. Lanza 403 si no."""
    user_id = get_jwt_identity()
    user = db.session.get(User, user_id)
    if not user or user.role != 'admin':
        raise AuthorizationError('Admin access required')
    return user


def _paginate_params():
    page = max(1, request.args.get('page', 1, type=int))
    per_page = min(50, max(5, request.args.get('per_page', 20, type=int)))
    return page, per_page


# ══════════════════════════════════════════════════════════════════════════════
#  DASHBOARD
# ══════════════════════════════════════════════════════════════════════════════

@admin_bp.route('/dashboard', methods=['GET'])
@jwt_required()
def dashboard():
    """
    Resumen general del sistema.
    Retorna: usuarios, transacciones, disputas pendientes y volumen total.
    """
    _require_admin()

    total_users = User.query.count()
    active_users = User.query.filter_by(is_active=True).count()
    total_txns = Transaction.query.count()
    completed_txns = Transaction.query.filter_by(status='completed').count()
    pending_disputes = Dispute.query.filter(
        Dispute.status.in_(('open', 'under_review'))
    ).count()
    resolved_disputes = Dispute.query.filter_by(status='resolved').count()

    total_volume = db.session.query(
        func.sum(Transaction.amount_to)
    ).filter_by(status='completed').scalar() or 0

    return {
        'users': {
            'total':  total_users,
            'active': active_users,
        },
        'transactions': {
            'total':     total_txns,
            'completed': completed_txns,
        },
        'disputes': {
            'pending':  pending_disputes,
            'resolved': resolved_disputes,
        },
        'total_volume': float(total_volume),
    }, 200


# ══════════════════════════════════════════════════════════════════════════════
#  USUARIOS
# ══════════════════════════════════════════════════════════════════════════════

@admin_bp.route('/users', methods=['GET'])
@jwt_required()
def list_users():
    """
    Listado paginado de todos los usuarios.

    Query params:
        page, per_page
        role     → filtrar por rol (buyer, vendor, admin)
        active   → '1' = activos, '0' = inactivos
    """
    _require_admin()
    page, per_page = _paginate_params()

    query = User.query
    role_filter = request.args.get('role')
    active_filter = request.args.get('active')

    if role_filter:
        query = query.filter_by(role=role_filter)
    if active_filter is not None:
        query = query.filter_by(is_active=(active_filter == '1'))

    pagination = query.order_by(User.created_at.desc()).paginate(
        page=page, per_page=per_page, error_out=False
    )

    return {
        'users': [
            {
                'id':                 u.id,
                'email':              u.email,
                'full_name':          u.full_name,
                'phone':              u.phone,
                'role':               u.role,
                'kyc_verified':       u.kyc_verified,
                'rating':             u.rating,
                'total_transactions': u.total_transactions,
                'is_active':          u.is_active,
                'is_banned':          u.is_banned,
                'created_at':         u.created_at.isoformat(),
            }
            for u in pagination.items
        ],
        'pagination': {
            'page':     pagination.page,
            'per_page': pagination.per_page,
            'total':    pagination.total,
            'pages':    pagination.pages,
        }
    }, 200


@admin_bp.route('/users/<user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    """Detalle completo de un usuario."""
    _require_admin()
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')

    # Estadísticas básicas
    total_disputes = (
        db.session.query(func.count(Dispute.id))
        .join(Transaction, Dispute.transaction_id == Transaction.id)
        .filter(
            (Transaction.buyer_id == user_id) |
            (Transaction.vendor_id == user_id)
        ).scalar() or 0
    )

    return {
        **user.to_dict(),
        'is_banned':      user.is_banned,
        'created_at':     user.created_at.isoformat(),
        'total_disputes': total_disputes,
    }, 200


@admin_bp.route('/users/<user_id>/ban', methods=['PATCH'])
@jwt_required()
def ban_user(user_id):
    """
    Banea o desbanea a un usuario.
    Body: { "banned": true | false }
    """
    _require_admin()
    user = db.session.get(User, user_id)
    if not user:
        raise NotFoundError('User not found')

    data = request.get_json() or {}
    banned = data.get('banned')
    if banned is None:
        raise AppException('MISSING_FIELD', '"banned" field is required', 400)

    user.is_banned = bool(banned)
    user.is_active = not bool(banned)

    if user.is_banned:
        notify(
            user_id=user.id,
            type='security',
            title='Cuenta suspendida',
            body='Tu cuenta ha sido suspendida por el administrador. Contacta soporte si crees que es un error.',
        )
    else:
        notify(
            user_id=user.id,
            type='security',
            title='Cuenta reactivada',
            body='Tu cuenta ha sido reactivada. Ya puedes operar con normalidad en la plataforma.',
        )

    db.session.commit()

    action = 'banned' if user.is_banned else 'unbanned'
    return {'message': f'User {action}', 'user_id': user_id, 'is_banned': user.is_banned}, 200


# ══════════════════════════════════════════════════════════════════════════════
#  DISPUTAS
# ══════════════════════════════════════════════════════════════════════════════

@admin_bp.route('/disputes', methods=['GET'])
@jwt_required()
def list_disputes():
    """
    Listado paginado de disputas para el panel admin.

    Query params:
        page, per_page
        status → open | under_review | resolved | closed (default: open + under_review)
    """
    _require_admin()
    page, per_page = _paginate_params()
    status_filter = request.args.get('status')   # None → devuelve open + under_review

    pagination = DisputeService.list_disputes_admin(page, per_page, status_filter)

    return {
        'disputes': [
            d.to_dict(include_transaction=True) for d in pagination.items
        ],
        'pagination': {
            'page':     pagination.page,
            'per_page': pagination.per_page,
            'total':    pagination.total,
            'pages':    pagination.pages,
            'has_next': pagination.has_next,
            'has_prev': pagination.has_prev,
        }
    }, 200


@admin_bp.route('/disputes/<dispute_id>', methods=['GET'])
@jwt_required()
def get_dispute(dispute_id):
    """Detalle completo de una disputa (admin)."""
    admin = _require_admin()
    dispute = DisputeService.get_dispute_detail(admin.id, dispute_id)
    return dispute.to_dict(include_transaction=True), 200


@admin_bp.route('/disputes/<dispute_id>/take', methods=['PATCH'])
@jwt_required()
def take_dispute(dispute_id):
    """
    Admin toma una disputa para revisión.
    Cambia el estado de 'open' a 'under_review' y asigna el admin.
    """
    admin = _require_admin()
    dispute = DisputeService.take_dispute(admin.id, dispute_id)

    # Notificar a ambas partes que la disputa está siendo revisada
    if dispute.transaction:
        for uid in {dispute.transaction.buyer_id, dispute.transaction.vendor_id}:
            notify(
                user_id=uid,
                type='dispute',
                title='Disputa en revisión',
                body='Un administrador ha tomado tu disputa y está revisando el caso. Te notificaremos con la resolución.',
                resource_id=dispute.id,
            )
    db.session.commit()

    return {
        'message':     'Dispute is now under review',
        'dispute_id':  dispute.id,
        'status':      dispute.status,
        'reviewed_by': admin.id,
    }, 200


@admin_bp.route('/disputes/<dispute_id>/resolve', methods=['PATCH'])
@jwt_required()
def resolve_dispute(dispute_id):
    """
    Admin resuelve una disputa.

    Body (JSON):
        resolution      (str, requerido) → 'favour_buyer' | 'favour_vendor'
        resolution_note (str, opcional)  → observaciones del admin

    Efecto sobre la transacción:
        favour_buyer  → txn.status = 'completed'
        favour_vendor → txn.status = 'cancelled'
    """
    admin = _require_admin()
    data = request.get_json() or {}

    resolution = data.get('resolution')
    if not resolution:
        raise AppException('MISSING_FIELD', '"resolution" field is required', 400)

    resolution_note = data.get('resolution_note')

    dispute = DisputeService.resolve_dispute(
        admin_id=admin.id,
        dispute_id=dispute_id,
        resolution=resolution,
        resolution_note=resolution_note,
    )

    # Notificar a comprador y vendedor con el resultado
    if dispute.transaction:
        txn = dispute.transaction
        favour_buyer = resolution == 'favour_buyer'
        notify(
            user_id=txn.buyer_id,
            type='dispute',
            title='Disputa resuelta' + (' — A tu favor ✓' if favour_buyer else ' — En contra'),
            body=(
                'La disputa fue resuelta a tu favor. La transacción fue completada.'
                if favour_buyer else
                'La disputa fue resuelta en favor del vendedor. La transacción fue cancelada.'
            ) + (f' Nota: {resolution_note}' if resolution_note else ''),
            resource_id=dispute.id,
        )
        notify(
            user_id=txn.vendor_id,
            type='dispute',
            title='Disputa resuelta' + (' — En contra' if favour_buyer else ' — A tu favor ✓'),
            body=(
                'La disputa fue resuelta en favor del comprador. La transacción fue completada.'
                if favour_buyer else
                'La disputa fue resuelta a tu favor. La transacción fue cancelada.'
            ) + (f' Nota: {resolution_note}' if resolution_note else ''),
            resource_id=dispute.id,
        )
        db.session.commit()

    return {
        'message':         'Dispute resolved',
        'dispute_id':      dispute.id,
        'status':          dispute.status,
        'resolution':      dispute.resolution,
        'resolution_note': dispute.resolution_note,
        'resolved_at':     dispute.resolved_at.isoformat() if dispute.resolved_at else None,
        'transaction_status': dispute.transaction.status if dispute.transaction else None,
    }, 200

# ══════════════════════════════════════════════════════════════════════════════
#  RECLAMOS
# ══════════════════════════════════════════════════════════════════════════════

@admin_bp.route('/complaints', methods=['GET'])
@jwt_required()
def list_complaints():
    _require_admin()
    page, per_page = _paginate_params()
    status_filter = request.args.get('status')

    from app.models.complaint import Complaint
    query = Complaint.query
    if status_filter:
        query = query.filter_by(status=status_filter)

    pagination = query.order_by(Complaint.created_at.desc()).paginate(
        page=page, per_page=per_page, error_out=False
    )

    return {
        'complaints': [c.to_dict() for c in pagination.items],
        'pagination': {
            'page':     pagination.page,
            'per_page': pagination.per_page,
            'total':    pagination.total,
            'pages':    pagination.pages,
            'has_next': pagination.has_next,
            'has_prev': pagination.has_prev,
        }
    }, 200


@admin_bp.route('/complaints/<complaint_id>', methods=['GET'])
@jwt_required()
def get_complaint(complaint_id):
    _require_admin()
    from app.models.complaint import Complaint
    complaint = db.session.get(Complaint, complaint_id)
    if not complaint:
        raise NotFoundError('Complaint not found')
    return complaint.to_dict(), 200


@admin_bp.route('/complaints/<complaint_id>/resolve', methods=['PATCH'])
@jwt_required()
def resolve_complaint(complaint_id):
    _require_admin()
    from app.models.complaint import Complaint
    complaint = db.session.get(Complaint, complaint_id)
    if not complaint:
        raise NotFoundError('Complaint not found')

    if complaint.status in ('resolved', 'closed'):
        raise AppException('INVALID_STATE', 'Complaint is already resolved', 400)

    data = request.get_json() or {}
    admin_note = data.get('admin_note')
    if not admin_note or not admin_note.strip():
        raise AppException('MISSING_FIELD', 'admin_note is required', 400)

    complaint.status = 'resolved'
    complaint.admin_note = admin_note.strip()

    notify(
        user_id=complaint.user_id,
        type='admin',
        title='Reclamo resuelto',
        body=f'Tu reclamo fue revisado y resuelto por el administrador. Respuesta: {admin_note.strip()}',
        resource_id=complaint.id,
    )

    db.session.commit()
    return complaint.to_dict(), 200
