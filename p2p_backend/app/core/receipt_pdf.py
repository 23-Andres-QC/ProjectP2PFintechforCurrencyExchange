from datetime import datetime
from io import BytesIO

import requests
from PIL import Image
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    Image as PdfImage,
    KeepTogether,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
)


PRIMARY = colors.HexColor("#19376D")
PRIMARY_DARK = colors.HexColor("#102A55")
LIGHT_BG = colors.HexColor("#F6F8FB")
BORDER = colors.HexColor("#D3DAE6")
TEXT = colors.HexColor("#172033")
MUTED = colors.HexColor("#64748B")
SUCCESS_BG = colors.HexColor("#E9F8EF")
SUCCESS = colors.HexColor("#047857")


def _money(value, prefix: str = "") -> str:
    try:
        return f"{prefix}{float(value):,.2f}"
    except (TypeError, ValueError):
        return f"{prefix}{value or '0.00'}"


def _rate(value) -> str:
    try:
        return f"{float(value):,.4f}"
    except (TypeError, ValueError):
        return str(value or "-")


def _status_label(value: str | None) -> str:
    labels = {
        "completed": "Completada",
        "closed": "Completada",
        "voucher_uploaded": "En verificacion",
        "accepted": "Aceptada",
        "pending": "Pendiente",
        "cancelled": "Cancelada",
        "disputed": "En disputa",
    }
    return labels.get((value or "").lower(), value or "Completada")


def _download_image(url: str | None) -> BytesIO | None:
    if not url:
        return None
    try:
        response = requests.get(url, timeout=12)
        response.raise_for_status()
        raw = BytesIO(response.content)
        image = Image.open(raw)
        image.load()

        normalized = BytesIO()
        if image.mode in ("RGBA", "P"):
            background = Image.new("RGB", image.size, "white")
            if image.mode == "P":
                image = image.convert("RGBA")
            background.paste(image, mask=image.split()[-1] if image.mode == "RGBA" else None)
            image = background
        else:
            image = image.convert("RGB")
        image.save(normalized, format="JPEG", quality=88)
        normalized.seek(0)
        return normalized
    except Exception:
        return None


def _scaled_pdf_image(image_data: BytesIO, max_w: float, max_h: float) -> PdfImage:
    image = Image.open(BytesIO(image_data.getvalue()))
    width, height = image.size
    scale = min(max_w / width, max_h / height)
    return PdfImage(image_data, width=width * scale, height=height * scale)


def _voucher_block(title: str, image_url: str | None, styles) -> KeepTogether:
    image_data = _download_image(image_url)
    elements = [
        Paragraph(title, styles["PESectionTitle"]),
        Spacer(1, 5 * mm),
    ]
    if image_data is None:
        elements.append(
            Table(
                [[Paragraph("Comprobante no disponible para imprimir en este documento.", styles["PEMuted"])]],
                colWidths=[170 * mm],
                style=[
                    ("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#FFF7ED")),
                    ("BOX", (0, 0), (-1, -1), 0.8, colors.HexColor("#FDBA74")),
                    ("LEFTPADDING", (0, 0), (-1, -1), 10),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                    ("TOPPADDING", (0, 0), (-1, -1), 10),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
                ],
            )
        )
    else:
        image = _scaled_pdf_image(image_data, max_w=170 * mm, max_h=160 * mm)
        elements.append(
            Table(
                [[image]],
                colWidths=[180 * mm],
                style=[
                    ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                    ("BACKGROUND", (0, 0), (-1, -1), colors.white),
                    ("BOX", (0, 0), (-1, -1), 0.8, BORDER),
                    ("LEFTPADDING", (0, 0), (-1, -1), 8),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 8),
                    ("TOPPADDING", (0, 0), (-1, -1), 8),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
                ],
            )
        )
    elements.append(Spacer(1, 9 * mm))
    return KeepTogether(elements)


def _build_styles():
    base = getSampleStyleSheet()
    base.add(ParagraphStyle("PEBrand", fontName="Helvetica-Bold", fontSize=20, textColor=colors.white, leading=24))
    base.add(ParagraphStyle("PEHeaderSmall", fontName="Helvetica", fontSize=9, textColor=colors.HexColor("#D7ECFF"), leading=12))
    base.add(ParagraphStyle("PETitle", fontName="Helvetica-Bold", fontSize=15, textColor=TEXT, leading=18))
    base.add(ParagraphStyle("PESectionTitle", fontName="Helvetica-Bold", fontSize=13, textColor=PRIMARY_DARK, leading=16))
    base.add(ParagraphStyle("PELabel", fontName="Helvetica", fontSize=9, textColor=MUTED, leading=12))
    base.add(ParagraphStyle("PEValue", fontName="Helvetica-Bold", fontSize=10.5, textColor=TEXT, leading=13))
    base.add(ParagraphStyle("PEMuted", fontName="Helvetica", fontSize=9.5, textColor=MUTED, leading=13))
    base.add(ParagraphStyle("PECenterMuted", parent=base["PEMuted"], alignment=TA_CENTER))
    return base


def build_receipt_pdf(transaction: dict) -> bytes:
    buffer = BytesIO()
    doc = SimpleDocTemplate(
        buffer,
        pagesize=A4,
        rightMargin=16 * mm,
        leftMargin=16 * mm,
        topMargin=14 * mm,
        bottomMargin=14 * mm,
        title="Voucher PeruExchange",
    )
    styles = _build_styles()

    txn_id = transaction.get("id", "")
    short_id = txn_id[:8].upper() if txn_id else "-"
    generated_at = datetime.utcnow().strftime("%d/%m/%Y %H:%M UTC")
    buyer_voucher_url = transaction.get("buyer_voucher_url")
    seller_voucher_url = transaction.get("seller_voucher_url") or transaction.get("vendor_voucher_url")

    story = []
    story.append(
        Table(
            [[
                Paragraph("PeruExchange", styles["PEBrand"]),
                Paragraph(f"Voucher #{short_id}<br/>{generated_at}", styles["PEHeaderSmall"]),
            ]],
            colWidths=[115 * mm, 65 * mm],
            style=[
                ("BACKGROUND", (0, 0), (-1, -1), PRIMARY),
                ("LEFTPADDING", (0, 0), (-1, -1), 12),
                ("RIGHTPADDING", (0, 0), (-1, -1), 12),
                ("TOPPADDING", (0, 0), (-1, -1), 13),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 13),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("ALIGN", (1, 0), (1, 0), "RIGHT"),
            ],
        )
    )
    story.append(Spacer(1, 8 * mm))

    status = _status_label(transaction.get("status"))
    story.append(
        Table(
            [[
                Paragraph("Comprobante oficial de operacion P2P", styles["PETitle"]),
                Paragraph(status.upper(), ParagraphStyle("PEBadge", fontName="Helvetica-Bold", fontSize=9, textColor=SUCCESS, alignment=TA_CENTER)),
            ], [
                Paragraph(f"Operacion: {txn_id}", styles["PEMuted"]),
                "",
            ]],
            colWidths=[130 * mm, 50 * mm],
            style=[
                ("BACKGROUND", (0, 0), (-1, -1), colors.white),
                ("BOX", (0, 0), (-1, -1), 0.8, BORDER),
                ("BACKGROUND", (1, 0), (1, 0), SUCCESS_BG),
                ("BOX", (1, 0), (1, 0), 0.8, colors.HexColor("#A7DDB6")),
                ("LEFTPADDING", (0, 0), (-1, -1), 10),
                ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                ("TOPPADDING", (0, 0), (-1, -1), 10),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ],
        )
    )
    story.append(Spacer(1, 7 * mm))

    rows = [
        ["Comprador", transaction.get("buyer_name") or "Comprador"],
        ["Vendedor", transaction.get("vendor_name") or "Vendedor"],
        ["Monto comprado", f"{_money(transaction.get('amount_from'))} USD"],
        ["Monto pagado", _money(transaction.get("amount_to"), "S/ ")],
        ["Tipo de cambio", f"S/ {_rate(transaction.get('exchange_rate'))}"],
        ["Estado", status],
    ]
    summary_rows = [[Paragraph(label, styles["PELabel"]), Paragraph(value, styles["PEValue"])] for label, value in rows]
    story.append(Paragraph("Resumen de la compra", styles["PESectionTitle"]))
    story.append(Spacer(1, 3 * mm))
    story.append(
        Table(
            summary_rows,
            colWidths=[58 * mm, 122 * mm],
            style=[
                ("BACKGROUND", (0, 0), (-1, -1), colors.white),
                ("BOX", (0, 0), (-1, -1), 0.8, BORDER),
                ("INNERGRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#E9EEF5")),
                ("LEFTPADDING", (0, 0), (-1, -1), 9),
                ("RIGHTPADDING", (0, 0), (-1, -1), 9),
                ("TOPPADDING", (0, 0), (-1, -1), 8),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ],
        )
    )
    story.append(Spacer(1, 8 * mm))

    story.append(
        Table(
            [[Paragraph("Este documento acredita que la operacion fue completada correctamente en PeruExchange. Los comprobantes se imprimen a continuacion para respaldo de la compra.", styles["PEMuted"])]],
            colWidths=[180 * mm],
            style=[
                ("BACKGROUND", (0, 0), (-1, -1), LIGHT_BG),
                ("BOX", (0, 0), (-1, -1), 0.8, BORDER),
                ("LEFTPADDING", (0, 0), (-1, -1), 10),
                ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                ("TOPPADDING", (0, 0), (-1, -1), 9),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 9),
            ],
        )
    )
    story.append(Spacer(1, 9 * mm))
    story.append(_voucher_block("Voucher del comprador", buyer_voucher_url, styles))
    story.append(_voucher_block("Comprobante del vendedor", seller_voucher_url, styles))
    story.append(Paragraph("PeruExchange - Intercambio P2P, Lima, Peru", styles["PECenterMuted"]))

    doc.build(story)
    return buffer.getvalue()
