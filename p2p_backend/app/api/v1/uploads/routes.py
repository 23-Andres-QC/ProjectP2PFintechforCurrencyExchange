import os
import base64
import uuid
from flask import Blueprint, request, send_from_directory, current_app
from flask_jwt_extended import jwt_required

uploads_bp = Blueprint('uploads', __name__, url_prefix='/uploads')

UPLOAD_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..', '..', 'static', 'uploads')


def _ensure_dir():
    os.makedirs(UPLOAD_DIR, exist_ok=True)


@uploads_bp.route('', methods=['POST'])
@uploads_bp.route('/', methods=['POST'])
@jwt_required()
def upload_image():
    _ensure_dir()
    data = request.get_json() or {}
    image_b64 = data.get('image_base64', '')
    if not image_b64:
        return {'error': {'code': 'NO_IMAGE', 'message': 'Se requiere image_base64'}}, 400

    if ',' in image_b64:
        image_b64 = image_b64.split(',', 1)[1]

    try:
        image_bytes = base64.b64decode(image_b64)
    except Exception:
        return {'error': {'code': 'INVALID_IMAGE', 'message': 'Imagen base64 inválida'}}, 400

    filename = f"{uuid.uuid4()}.jpg"
    filepath = os.path.join(UPLOAD_DIR, filename)
    with open(filepath, 'wb') as f:
        f.write(image_bytes)

    return {'url': f'/api/v1/uploads/{filename}'}, 201


@uploads_bp.route('/<filename>', methods=['GET'])
def serve_image(filename):
    _ensure_dir()
    return send_from_directory(UPLOAD_DIR, filename)
