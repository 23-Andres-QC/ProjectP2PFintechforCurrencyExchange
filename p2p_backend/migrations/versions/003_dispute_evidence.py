from alembic import op
import sqlalchemy as sa


revision = '003_dispute_evidence'
down_revision = '002_auditoria_flujos_criticos'
branch_labels = None
depends_on = None


def upgrade():
    with op.batch_alter_table('disputes', schema=None) as batch_op:
        batch_op.add_column(sa.Column('evidence_url', sa.String(length=500), nullable=True))


def downgrade():
    with op.batch_alter_table('disputes', schema=None) as batch_op:
        batch_op.drop_column('evidence_url')
