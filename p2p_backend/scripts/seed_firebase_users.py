"""
Script para crear los usuarios seed en Firebase Authentication.
Ejecutar desde la carpeta raiz del backend:
  python scripts/seed_firebase_users.py
"""
import os
import sys

CREDENTIALS_PATH = os.environ.get(
    'FIREBASE_CREDENTIALS_PATH',
    os.path.join(os.path.dirname(__file__), '..', 'docker', 'firebase-credentials.json')
)

SEED_USERS = [
    {'email': 'comprador@peruexchange.com', 'password': 'Comprador123!', 'display_name': 'Comprador Demo'},
    {'email': 'vendedor@peruexchange.com',  'password': 'Vendedor123!',  'display_name': 'Vendedor Demo'},
    {'email': 'admin@peruexchange.com',     'password': 'Admin123!',     'display_name': 'Admin Demo'},
]

def main():
    try:
        import firebase_admin
        from firebase_admin import credentials, auth
    except ImportError:
        print('ERROR: firebase-admin no instalado. Ejecuta: pip install firebase-admin')
        sys.exit(1)

    if not os.path.exists(CREDENTIALS_PATH):
        print(f'ERROR: No se encontró el archivo de credenciales en:\n  {CREDENTIALS_PATH}')
        print('Descarga la clave privada desde Firebase Console -> Cuentas de servicio')
        sys.exit(1)

    if not firebase_admin._apps:
        cred = credentials.Certificate(CREDENTIALS_PATH)
        firebase_admin.initialize_app(cred)

    print('Creando usuarios seed en Firebase Authentication...\n')

    for user_data in SEED_USERS:
        email = user_data['email']
        try:
            existing = auth.get_user_by_email(email)
            print(f'  ✓ Ya existe: {email} (uid: {existing.uid})')
        except auth.UserNotFoundError:
            try:
                new_user = auth.create_user(
                    email=email,
                    password=user_data['password'],
                    display_name=user_data['display_name'],
                    email_verified=True
                )
                print(f'  ✓ Creado: {email} (uid: {new_user.uid})')
            except Exception as e:
                print(f'  ✗ Error creando {email}: {e}')

    print('\nListo. Ahora puedes hacer login con estos usuarios en la app.')

if __name__ == '__main__':
    main()
