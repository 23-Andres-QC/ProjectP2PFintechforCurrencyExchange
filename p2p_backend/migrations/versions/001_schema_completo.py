"""Migración única consolidada — esquema completo P2P Exchange

Revision ID: 001_schema_completo
Revises:
Create Date: 2026-06-08

Consolida todo el historial de migraciones en una sola versión:
  - Corrección de nombres de índices y foreign keys (c63adeb0b02f)
  - Campos de resolución en disputes (008_disputes_resolution)

Nota: Las tablas complaints y notifications ya se crean mediante los scripts
SQL en migrations/sql/ (docker-entrypoint-initdb.d). Por eso no se replican
aquí para evitar el error "table already exists".
"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision = '001_schema_completo'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    # ── Corrección de índices y constraints (antes: c63adeb0b02f) ─────────────

    with op.batch_alter_table('audit_logs', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))

    with op.batch_alter_table('bank_accounts', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.drop_index(batch_op.f('idx_bank_accounts_user_id'))
        batch_op.create_index(batch_op.f('ix_bank_accounts_user_id'), ['user_id'], unique=False)
        batch_op.drop_constraint(batch_op.f('bank_accounts_user_id_fkey'), type_='foreignkey')
        batch_op.create_foreign_key(None, 'users', ['user_id'], ['id'])

    with op.batch_alter_table('disputes', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.drop_index(batch_op.f('idx_disputes_transaction_id'))
        batch_op.create_index(batch_op.f('ix_disputes_transaction_id'), ['transaction_id'], unique=False)

    with op.batch_alter_table('offers', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.drop_index(batch_op.f('idx_offers_status'))
        batch_op.drop_index(batch_op.f('idx_offers_vendor_id'))
        batch_op.create_index(batch_op.f('ix_offers_status'), ['status'], unique=False)
        batch_op.create_index(batch_op.f('ix_offers_vendor_id'), ['vendor_id'], unique=False)
        batch_op.drop_constraint(batch_op.f('offers_vendor_id_fkey'), type_='foreignkey')
        batch_op.create_foreign_key(None, 'users', ['vendor_id'], ['id'])

    with op.batch_alter_table('transactions', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.drop_index(batch_op.f('idx_transactions_buyer_id'))
        batch_op.drop_index(batch_op.f('idx_transactions_status'))
        batch_op.drop_index(batch_op.f('idx_transactions_vendor_id'))
        batch_op.create_index(batch_op.f('ix_transactions_buyer_id'), ['buyer_id'], unique=False)
        batch_op.create_index(batch_op.f('ix_transactions_status'), ['status'], unique=False)
        batch_op.create_index(batch_op.f('ix_transactions_vendor_id'), ['vendor_id'], unique=False)
        batch_op.drop_constraint(batch_op.f('transactions_vendor_id_fkey'), type_='foreignkey')
        batch_op.drop_constraint(batch_op.f('transactions_buyer_id_fkey'), type_='foreignkey')
        batch_op.create_foreign_key(None, 'users', ['vendor_id'], ['id'])
        batch_op.create_foreign_key(None, 'users', ['buyer_id'], ['id'])

    with op.batch_alter_table('users', schema=None) as batch_op:
        batch_op.alter_column('created_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.alter_column('updated_at',
               existing_type=postgresql.TIMESTAMP(),
               nullable=False,
               existing_server_default=sa.text('CURRENT_TIMESTAMP'))
        batch_op.drop_index(batch_op.f('idx_users_email'))
        batch_op.drop_constraint(batch_op.f('users_email_key'), type_='unique')
        batch_op.create_index(batch_op.f('ix_users_email'), ['email'], unique=True)

    # ── Campos de resolución en disputes (antes: 008_disputes_resolution) ─────

    with op.batch_alter_table('disputes') as batch_op:
        batch_op.add_column(
            sa.Column('resolved_by', sa.String(36),
                      sa.ForeignKey('users.id'), nullable=True)
        )
        batch_op.add_column(
            sa.Column('resolution', sa.String(20), nullable=True)
        )
        batch_op.add_column(
            sa.Column('resolution_note', sa.Text, nullable=True)
        )
        batch_op.add_column(
            sa.Column('resolved_at', sa.DateTime, nullable=True)
        )
        batch_op.create_index('idx_disputes_status', ['status'])


def downgrade():
    # ── Revertir resolución en disputes ──────────────────────────────────────
    with op.batch_alter_table('disputes') as batch_op:
        batch_op.drop_index('idx_disputes_status')
        batch_op.drop_column('resolved_at')
        batch_op.drop_column('resolution_note')
        batch_op.drop_column('resolution')
        batch_op.drop_column('resolved_by')

    # ── Revertir correcciones de índices ─────────────────────────────────────
    with op.batch_alter_table('users', schema=None) as batch_op:
        batch_op.drop_index(batch_op.f('ix_users_email'))
        batch_op.create_unique_constraint(batch_op.f('users_email_key'), ['email'])
        batch_op.create_index(batch_op.f('idx_users_email'), ['email'], unique=False)

    with op.batch_alter_table('transactions', schema=None) as batch_op:
        batch_op.drop_constraint(None, type_='foreignkey')
        batch_op.drop_constraint(None, type_='foreignkey')
        batch_op.create_foreign_key(batch_op.f('transactions_buyer_id_fkey'), 'users', ['buyer_id'], ['id'], ondelete='CASCADE')
        batch_op.create_foreign_key(batch_op.f('transactions_vendor_id_fkey'), 'users', ['vendor_id'], ['id'], ondelete='CASCADE')
        batch_op.drop_index(batch_op.f('ix_transactions_vendor_id'))
        batch_op.drop_index(batch_op.f('ix_transactions_status'))
        batch_op.drop_index(batch_op.f('ix_transactions_buyer_id'))
        batch_op.create_index(batch_op.f('idx_transactions_vendor_id'), ['vendor_id'], unique=False)
        batch_op.create_index(batch_op.f('idx_transactions_status'), ['status'], unique=False)
        batch_op.create_index(batch_op.f('idx_transactions_buyer_id'), ['buyer_id'], unique=False)

    with op.batch_alter_table('offers', schema=None) as batch_op:
        batch_op.drop_constraint(None, type_='foreignkey')
        batch_op.create_foreign_key(batch_op.f('offers_vendor_id_fkey'), 'users', ['vendor_id'], ['id'], ondelete='CASCADE')
        batch_op.drop_index(batch_op.f('ix_offers_vendor_id'))
        batch_op.drop_index(batch_op.f('ix_offers_status'))
        batch_op.create_index(batch_op.f('idx_offers_vendor_id'), ['vendor_id'], unique=False)
        batch_op.create_index(batch_op.f('idx_offers_status'), ['status'], unique=False)

    with op.batch_alter_table('disputes', schema=None) as batch_op:
        batch_op.drop_index(batch_op.f('ix_disputes_transaction_id'))
        batch_op.create_index(batch_op.f('idx_disputes_transaction_id'), ['transaction_id'], unique=False)

    with op.batch_alter_table('bank_accounts', schema=None) as batch_op:
        batch_op.drop_constraint(None, type_='foreignkey')
        batch_op.create_foreign_key(batch_op.f('bank_accounts_user_id_fkey'), 'users', ['user_id'], ['id'], ondelete='CASCADE')
        batch_op.drop_index(batch_op.f('ix_bank_accounts_user_id'))
        batch_op.create_index(batch_op.f('idx_bank_accounts_user_id'), ['user_id'], unique=False)
