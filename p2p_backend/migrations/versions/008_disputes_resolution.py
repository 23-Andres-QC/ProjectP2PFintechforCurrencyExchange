"""add resolution fields to disputes

Revision ID: 008_disputes_resolution
Revises: c63adeb0b02f
Create Date: 2026-06-03

Agrega los campos de resolución a la tabla disputes:
  - resolved_by     → FK al admin que resolvió
  - resolution      → 'favour_buyer' | 'favour_vendor'
  - resolution_note → observaciones del admin
  - resolved_at     → timestamp de resolución
  - index en status → acelera la query del panel admin
"""
from alembic import op
import sqlalchemy as sa

revision = '008_disputes_resolution'
down_revision = 'c63adeb0b02f'
branch_labels = None
depends_on = None


def upgrade():
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
    with op.batch_alter_table('disputes') as batch_op:
        batch_op.drop_index('idx_disputes_status')
        batch_op.drop_column('resolved_at')
        batch_op.drop_column('resolution_note')
        batch_op.drop_column('resolution')
        batch_op.drop_column('resolved_by')
