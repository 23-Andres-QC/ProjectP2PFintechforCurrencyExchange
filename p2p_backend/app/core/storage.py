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


def upload_voucher(image_bytes: bytes, role: str) -> str:
    """
    Sube imagen al bucket 'vouchers' en Supabase.
    role: 'buyer' | 'seller'
    Retorna la URL pública de la imagen.
    """
    client = _get_client()
    path = f"{role}/{uuid.uuid4()}.jpg"
    client.storage.from_(BUCKET).upload(
        path=path,
        file=image_bytes,
        file_options={"content-type": "image/jpeg"}
    )
    return client.storage.from_(BUCKET).get_public_url(path)
