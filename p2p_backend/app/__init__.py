from flask import Flask
from flask_cors import CORS
from flask_migrate import Migrate


def create_app(config_name='development'):
    from app.core.config import config
    from app.core.database import db
    from app.core.security import jwt
    from app.core.rate_limit import limiter

    app = Flask(__name__)
    app.config.from_object(config[config_name])

    db.init_app(app)
    jwt.init_app(app)
    limiter.init_app(app)
    Migrate(app, db)
    CORS(app, resources={r"/api/*": {"origins": "*"}})

    with app.app_context():
        from app.models.user import User
        from app.models import (
            Currency, ExchangeRate, BankAccount, Offer,
            Transaction, Voucher, Rating, Dispute, AuditLog, Complaint
        )
        db.create_all()

        from app.api.v1 import api_v1
        app.register_blueprint(api_v1, url_prefix='/api/v1')

        @app.route('/health')
        def health():
            return {'status': 'healthy'}, 200

        from app.core.exceptions import register_error_handlers
        register_error_handlers(app, db)

    return app
