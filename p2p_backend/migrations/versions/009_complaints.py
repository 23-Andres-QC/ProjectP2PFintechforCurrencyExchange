"""add complaints table

Revision ID: 009_complaints
Revises: 008_disputes_resolution
Create Date: 2026-06-04
"""
from alembic import op
import sqlalchemy as sa

revision = '009_complaints'
down_revision = '008_disputes_resolution'
branch_labels = None
depends_on = None


def upgrade():
    op.create_table(
        'complaints',
        sa.Column('id', sa.String(36), primary_key=True),
        sa.Column('user_id', sa.String(36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('type', sa.String(50), nullable=False),
        sa.Column('description', sa.Text, nullable=False),
        sa.Column('status', sa.String(20), nullable=False, server_default='pending'),
        sa.Column('admin_note', sa.Text, nullable=True),
        sa.Column('created_at', sa.DateTime, nullable=False),
        sa.Column('updated_at', sa.DateTime, nullable=False),
    )
    op.create_index('idx_complaints_user_id', 'complaints', ['user_id'])
    op.create_index('idx_complaints_status', 'complaints', ['status'])


def downgrade():
    op.drop_index('idx_complaints_status')
    op.drop_index('idx_complaints_user_id')
    op.drop_table('complaints')