from flask import Blueprint, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.services.user_service import UserService

users_bp = Blueprint('users', __name__, url_prefix='/users')


@users_bp.route('/me', methods=['GET'])
@jwt_required()
def get_me():
    user_id = get_jwt_identity()
    user = UserService.get_by_id(user_id)
    return user.to_dict(), 200


@users_bp.route('/profile', methods=['PATCH'])
@jwt_required()
def update_profile():
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    user = UserService.update_profile(user_id, data)
    return user.to_dict(), 200


@users_bp.route('/kyc', methods=['POST'])
@jwt_required()
def submit_kyc():
    user_id = get_jwt_identity()
    user = UserService.get_by_id(user_id)

    required_files = {
        'dni_front': request.files.get('dni_front'),
        'dni_back': request.files.get('dni_back'),
        'selfie': request.files.get('selfie'),
    }
    missing = [name for name, file in required_files.items() if not file]
    if missing:
        return {
            'error': {
                'code': 'MISSING_KYC_FILE',
                'message': f'Faltan archivos KYC: {", ".join(missing)}'
            }
        }, 400

    from app.core.storage import upload_kyc_document

    document_urls = {
        'dni_front_url': upload_kyc_document(required_files['dni_front'].read(), user.email, 'dni_front'),
        'dni_back_url': upload_kyc_document(required_files['dni_back'].read(), user.email, 'dni_back'),
        'selfie_url': upload_kyc_document(required_files['selfie'].read(), user.email, 'selfie'),
    }

    signature_file = request.files.get('signature')
    if signature_file:
        document_urls['signature_url'] = upload_kyc_document(signature_file.read(), user.email, 'signature')

    updated_user = UserService.submit_kyc(user_id, document_urls)
    return {
        'message': 'KYC recibido y en revision',
        'kyc_status': updated_user.kyc_status,
        'kyc_verified': updated_user.kyc_verified,
        'documents_saved': True,
        'has_signature': bool(updated_user.signature_url),
    }, 200


@users_bp.route('/signature', methods=['POST'])
@jwt_required()
def submit_signature():
    user_id = get_jwt_identity()
    user = UserService.get_by_id(user_id)

    signature_file = request.files.get('signature')
    if not signature_file:
        return {
            'error': {'code': 'MISSING_SIGNATURE_FILE', 'message': 'Falta el archivo de firma'}
        }, 400

    from app.core.storage import upload_kyc_document

    signature_url = upload_kyc_document(signature_file.read(), user.email, 'signature')
    UserService.save_signature(user_id, signature_url)
    return {'message': 'Firma guardada', 'signature_url': signature_url}, 200


@users_bp.route('/<user_id>', methods=['GET'])
def get_public_profile(user_id):
    return UserService.get_public_profile(user_id), 200
