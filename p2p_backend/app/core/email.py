import os
import smtplib
from email.message import EmailMessage
from flask import current_app


def send_voucher_email(
    to_email: str,
    buyer_name: str,
    transaction_id: str,
    pdf_url: str,
    pdf_bytes: bytes,
) -> bool:
    host = os.getenv('SMTP_HOST', 'smtp.gmail.com')
    port = int(os.getenv('SMTP_PORT', '587'))
    username = os.getenv('SMTP_USERNAME')
    password = os.getenv('SMTP_PASSWORD')
    sender = os.getenv('SMTP_FROM') or username

    if not username or not password or not sender or not to_email:
        current_app.logger.warning('SMTP email skipped: missing SMTP configuration or recipient')
        return False

    msg = EmailMessage()
    msg['Subject'] = 'Voucher de compra PeruExchange'
    msg['From'] = sender
    msg['To'] = to_email
    msg.set_content(
        f'Hola {buyer_name or "usuario"},\n\n'
        f'Tu operacion {transaction_id} fue completada.\n'
        f'Adjuntamos tu voucher en PDF.\n\n'
        f'Tambien puedes descargarlo aqui:\n{pdf_url}\n\n'
        'Gracias por usar PeruExchange.'
    )
    msg.add_alternative(
        f"""
        <p>Hola {buyer_name or "usuario"},</p>
        <p>Tu operacion <b>{transaction_id}</b> fue completada.</p>
        <p>Adjuntamos tu voucher en PDF.</p>
        <p>Tambien puedes descargarlo aqui:
            <a href="{pdf_url}">Descargar voucher</a>
        </p>
        <p>Gracias por usar PeruExchange.</p>
        """,
        subtype='html',
    )
    msg.add_attachment(
        pdf_bytes,
        maintype='application',
        subtype='pdf',
        filename=f'voucher-{transaction_id[:8]}.pdf',
    )

    try:
        with smtplib.SMTP(host, port, timeout=15) as smtp:
            smtp.starttls()
            smtp.login(username, password)
            smtp.send_message(msg)
        return True
    except Exception as exc:
        current_app.logger.warning('SMTP voucher email failed: %s', exc)
        return False
