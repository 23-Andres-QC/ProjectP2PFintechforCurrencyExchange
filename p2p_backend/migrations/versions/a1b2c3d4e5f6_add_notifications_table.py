"""add notifications table

Revision ID: a1b2c3d4e5f6
Revises: c63adeb0b02f
Create Date: 2026-06-04 00:00:00.000000

"""
from alembic import op
import sqlalchemy as sa

revision = 'a1b2c3d4e5f6'
down_revision = 'c63adeb0b02f'
branch_labels = None
depends_on = None


def upgrade():
    op.create_table(
        'notifications',
        sa.Column('id',          sa.String(36),  primary_key=True),
        sa.Column('user_id',     sa.String(36),  sa.ForeignKey('users.id'), nullable=False),
        sa.Column('type',        sa.String(50),  nullable=False),
        sa.Column('title',       sa.String(255), nullable=False),
        sa.Column('body',        sa.Text(),      nullable=False),
        sa.Column('is_read',     sa.Boolean(),   nullable=False, server_default=sa.text('false')),
        sa.Column('resource_id', sa.String(36),  nullable=True),
        sa.Column('created_at',  sa.DateTime(),  nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
        sa.Column('updated_at',  sa.DateTime(),  nullable=False, server_default=sa.text('CURRENT_TIMESTAMP')),
    )
    op.create_index('ix_notifications_user_id', 'notifications', ['user_id'])
    op.create_index('ix_notifications_is_read',  'notifications', ['is_read'])


def downgrade():
    op.drop_index('ix_notifications_is_read',  table_name='notifications')
    op.drop_index('ix_notifications_user_id',  table_name='notifications')
    op.drop_table('notifications')
