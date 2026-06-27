import os
import uuid
from supabase import create_client

_client = None
BUCKET = 'vouchers'


def _get_client():
    global _client
    if _client is None:
        url = os.getenv('SUPABASE_URL')
        key = os.getenv('SUPABASE_SERVICE_KEY')
        if not url or not key:
            raise RuntimeError('SUPABASE_URL y SUPABASE_SERVICE_KEY deben estar en el .env')
        _client = create_client(url, key)
    return _client


def _safe_name(value: str) -> str:
    return value.replace('@', '_').replace('.', '_').replace('+', '_').replace('/', '_')


def upload_file(file_bytes: bytes, path: str, content_type: str) -> str:
    client = _get_client()
    client.storage.from_(BUCKET).upload(
        path=path,
        file=file_bytes,
        file_options={"content-type": content_type, "upsert": "true"},
    )
    return client.storage.from_(BUCKET).get_public_url(path)


def upload_voucher(image_bytes: bytes, user_email: str, transaction_id: str, role: str = 'buyer') -> str:
    """
    Sube voucher al bucket 'vouchers' en Supabase.
    Estructura: buyer|seller/{email_sanitizado}/{transaction_id}/{uuid}.jpg
    Retorna la URL publica de la imagen.
    """
    safe_role = role if role in ('buyer', 'seller') else 'buyer'
    path = f"{safe_role}/{_safe_name(user_email)}/{transaction_id}/{uuid.uuid4()}.jpg"
    return upload_file(image_bytes, path, "image/jpeg")


def upload_kyc_document(file_bytes: bytes, user_email: str, document_type: str) -> str:
    safe_type = document_type if document_type in ('dni_front', 'dni_back', 'selfie', 'signature') else 'document'
    path = f"kyc/{_safe_name(user_email)}/{safe_type}/{uuid.uuid4()}.jpg"
    return upload_file(file_bytes, path, "image/jpeg")


def upload_receipt_pdf(pdf_bytes: bytes, user_email: str, transaction_id: str) -> str:
    path = f"receipts/{_safe_name(user_email)}/{transaction_id}/voucher-compra.pdf"
    return upload_file(pdf_bytes, path, "application/pdf")
