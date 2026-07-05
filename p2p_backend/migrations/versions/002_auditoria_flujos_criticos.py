from alembic import op
import sqlalchemy as sa


revision = '002_auditoria_flujos_criticos'
down_revision = '001_schema_completo'
branch_labels = None
depends_on = None


def upgrade():
    with op.batch_alter_table('users', schema=None) as batch_op:
        batch_op.add_column(sa.Column('kyc_status', sa.String(length=20), nullable=False, server_default='approved'))
        batch_op.add_column(sa.Column('terms_accepted', sa.Boolean(), nullable=False, server_default=sa.false()))
        batch_op.add_column(sa.Column('terms_url', sa.String(length=500), nullable=True))
        batch_op.add_column(sa.Column('terms_version', sa.String(length=50), nullable=True))
        batch_op.add_column(sa.Column('terms_accepted_at', sa.DateTime(), nullable=True))

    with op.batch_alter_table('transactions', schema=None) as batch_op:
        batch_op.add_column(sa.Column('accepted_at', sa.DateTime(), nullable=True))
        batch_op.add_column(sa.Column('confirmed_at', sa.DateTime(), nullable=True))


def downgrade():
    with op.batch_alter_table('transactions', schema=None) as batch_op:
        batch_op.drop_column('confirmed_at')
        batch_op.drop_column('accepted_at')

    with op.batch_alter_table('users', schema=None) as batch_op:
        batch_op.drop_column('terms_accepted_at')
        batch_op.drop_column('terms_version')
        batch_op.drop_column('terms_url')
        batch_op.drop_column('terms_accepted')
        batch_op.drop_column('kyc_status')
