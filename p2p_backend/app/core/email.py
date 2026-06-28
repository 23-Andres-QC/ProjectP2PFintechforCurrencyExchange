import logging
import os
import smtplib
from email.message import EmailMessage

logger = logging.getLogger(__name__)


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
        logger.warning('SMTP email skipped: missing SMTP configuration or recipient')
        return False

    msg = EmailMessage()
    msg['Subject'] = f'PeruExchange | Voucher de operacion {transaction_id[:8].upper()}'
    msg['From'] = sender
    msg['To'] = to_email
    msg.set_content(
        f'Hola {buyer_name or "usuario"},\n\n'
        f'Tu operacion {transaction_id} fue completada correctamente.\n'
        'Adjuntamos el voucher oficial en PDF con los comprobantes impresos dentro del documento.\n\n'
        f'Descarga alternativa: {pdf_url}\n\n'
        'Gracias por usar PeruExchange.'
    )
    msg.add_alternative(
        f"""
        <div style="font-family:Arial,sans-serif;background:#f6f8fb;padding:24px;color:#172033;">
          <div style="max-width:560px;margin:0 auto;background:#ffffff;border:1px solid #d3dae6;border-radius:12px;overflow:hidden;">
            <div style="background:#19376d;color:#ffffff;padding:18px 22px;">
              <h2 style="margin:0;font-size:20px;">PeruExchange</h2>
              <p style="margin:6px 0 0;color:#d7ecff;font-size:13px;">Voucher oficial de operacion P2P</p>
            </div>
            <div style="padding:22px;">
              <p>Hola <b>{buyer_name or "usuario"}</b>,</p>
              <p>Tu operacion <b>{transaction_id}</b> fue completada correctamente.</p>
              <p>Adjuntamos el voucher oficial en PDF. El documento incluye el resumen de la compra y los comprobantes impresos dentro del mismo archivo.</p>
              <p style="margin:22px 0;">
                <a href="{pdf_url}" style="background:#19376d;color:#ffffff;text-decoration:none;padding:11px 16px;border-radius:8px;display:inline-block;font-weight:bold;">
                  Descargar voucher
                </a>
              </p>
              <p style="font-size:12px;color:#64748b;">Conserva este comprobante para tus registros.</p>
              <p>Gracias por usar PeruExchange.</p>
            </div>
          </div>
        </div>
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
        logger.warning('SMTP voucher email failed: %s', exc)
        return False
