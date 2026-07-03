from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision = '001_schema_completo'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
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

    # Nota: resolved_by/resolution/resolution_note/resolved_at + idx_disputes_status,
    # idx_audit_logs_user_id e idx_ratings_ratee_id ya vienen creados por los scripts SQL
    # de arranque (migrations/sql/09_disputes.sql, 10_audit_logs.sql, 08_ratings.sql).
    # Agregarlos aqui de nuevo rompia esta migracion en una base de datos nueva.


def downgrade():
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
