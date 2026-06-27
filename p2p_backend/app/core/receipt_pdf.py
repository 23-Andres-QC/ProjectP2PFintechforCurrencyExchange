from datetime import datetime


def _pdf_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def build_receipt_pdf(transaction: dict) -> bytes:
    lines = [
        "PeruExchange - Voucher de compra",
        f"Operacion: {transaction.get('id', '')}",
        f"Fecha: {datetime.utcnow().strftime('%Y-%m-%d %H:%M UTC')}",
        "",
        f"Comprador: {transaction.get('buyer_name') or 'Comprador'}",
        f"Vendedor: {transaction.get('vendor_name') or 'Vendedor'}",
        f"Monto comprado: {transaction.get('amount_from')} USD",
        f"Monto pagado: S/ {transaction.get('amount_to')}",
        f"Tasa: {transaction.get('exchange_rate')}",
        f"Estado: {transaction.get('status')}",
        "",
        f"Voucher comprador: {transaction.get('buyer_voucher_url') or 'No registrado'}",
        f"Comprobante vendedor: {transaction.get('seller_voucher_url') or transaction.get('vendor_voucher_url') or 'No registrado'}",
        "",
        "Este documento certifica que la operacion fue completada en la plataforma.",
    ]

    content = ["BT", "/F1 14 Tf", "50 780 Td"]
    first = True
    for line in lines:
        if not first:
            content.append("0 -22 Td")
        first = False
        content.append(f"({_pdf_escape(str(line))}) Tj")
    content.append("ET")
    stream = "\n".join(content).encode("latin-1", errors="replace")

    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        b"<< /Length " + str(len(stream)).encode() + b" >>\nstream\n" + stream + b"\nendstream",
    ]

    pdf = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for index, obj in enumerate(objects, start=1):
        offsets.append(len(pdf))
        pdf.extend(f"{index} 0 obj\n".encode())
        pdf.extend(obj)
        pdf.extend(b"\nendobj\n")

    xref_offset = len(pdf)
    pdf.extend(f"xref\n0 {len(objects) + 1}\n".encode())
    pdf.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        pdf.extend(f"{offset:010d} 00000 n \n".encode())
    pdf.extend(
        f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\nstartxref\n{xref_offset}\n%%EOF\n".encode()
    )
    return bytes(pdf)
