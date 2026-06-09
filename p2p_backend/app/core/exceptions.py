

class AppException(Exception):
    def __init__(self, code: str, message: str, status_code: int = 400):
        self.code = code
        self.message = message
        self.status_code = status_code
        super().__init__(self.message)


class ValidationError(AppException):
    def __init__(self, message: str):
        super().__init__('VALIDATION_ERROR', message, 400)


class AuthenticationError(AppException):
    def __init__(self, message: str = 'Unauthorized'):
        super().__init__('UNAUTHORIZED', message, 401)


class AuthorizationError(AppException):
    def __init__(self, message: str = 'Forbidden'):
        super().__init__('FORBIDDEN', message, 403)


class NotFoundError(AppException):
    def __init__(self, message: str = 'Resource not found'):
        super().__init__('NOT_FOUND', message, 404)


class ConflictError(AppException):
    def __init__(self, message: str = 'Conflict'):
        super().__init__('CONFLICT', message, 409)


def register_error_handlers(app, db):
    @app.errorhandler(AppException)
    def handle_app_exception(error):
        return {'error': {'code': error.code, 'message': error.message}}, error.status_code

    @app.errorhandler(400)
    def bad_request(e):
        return {'error': {'code': 'BAD_REQUEST', 'message': str(e)}}, 400

    @app.errorhandler(401)
    def unauthorized(e):
        return {'error': {'code': 'UNAUTHORIZED', 'message': str(e)}}, 401

    @app.errorhandler(403)
    def forbidden(e):
        return {'error': {'code': 'FORBIDDEN', 'message': str(e)}}, 403

    @app.errorhandler(404)
    def not_found(e):
        return {'error': {'code': 'NOT_FOUND', 'message': str(e)}}, 404

    @app.errorhandler(500)
    def internal_error(e):
        db.session.rollback()
        return {'error': {'code': 'INTERNAL_ERROR', 'message': str(e)}}, 500
