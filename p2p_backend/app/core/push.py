import os
import logging

logger = logging.getLogger(__name__)

_initialized = False


def _init_firebase():
    global _initialized
    if _initialized:
        return True
    try:
        import firebase_admin
        from firebase_admin import credentials
        cred_path = os.environ.get('FIREBASE_CREDENTIALS_PATH', '/app/firebase-credentials.json')
        if not os.path.exists(cred_path):
            logger.warning(f'Firebase credentials not found at {cred_path}. Push notifications disabled.')
            return False
        if not firebase_admin._apps:
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred)
        _initialized = True
        return True
    except Exception as e:
        logger.error(f'Firebase init failed: {e}')
        return False


def send_push(fcm_token: str, title: str, body: str, data: dict = None):
    if not fcm_token:
        return
    if not _init_firebase():
        return
    try:
        from firebase_admin import messaging
        message = messaging.Message(
            notification=messaging.Notification(title=title, body=body),
            data={k: str(v) for k, v in (data or {}).items()},
            token=fcm_token,
            android=messaging.AndroidConfig(priority='high'),
        )
        message_id = messaging.send(message)
        logger.info('Push notification sent: %s', message_id)
    except Exception as e:
        logger.warning(f'Push notification failed: {e}')
