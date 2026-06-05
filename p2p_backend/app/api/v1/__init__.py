"""API v1 — registra todos los blueprints bajo /api/v1"""
from flask import Blueprint

api_v1 = Blueprint('api_v1', __name__)

from app.api.v1.auth.routes import auth_bp
from app.api.v1.complaints.routes import complaints_bp
from app.api.v1.offers.routes import offers_bp
from app.api.v1.transactions.routes import transactions_bp
from app.api.v1.users.routes import users_bp
from app.api.v1.admin.routes import admin_bp
from app.api.v1.exchange.routes import exchange_bp
from app.api.v1.bank_accounts.routes import bank_accounts_bp
from app.api.v1.ratings.routes import ratings_bp
from app.api.v1.disputes.routes import disputes_bp  # ← agrega esto
from app.api.v1.notifications.routes import notifications_bp


api_v1.register_blueprint(auth_bp)
api_v1.register_blueprint(offers_bp)
api_v1.register_blueprint(transactions_bp)
api_v1.register_blueprint(users_bp)
api_v1.register_blueprint(admin_bp)
api_v1.register_blueprint(exchange_bp)
api_v1.register_blueprint(bank_accounts_bp)
api_v1.register_blueprint(ratings_bp)
api_v1.register_blueprint(disputes_bp)  # ← agrega esto
api_v1.register_blueprint(complaints_bp)
api_v1.register_blueprint(notifications_bp)
