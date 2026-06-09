from marshmallow import Schema, fields, validate, validates, ValidationError

CURRENCIES_SOPORTADAS = [
    "PEN", "USD", "EUR", "USDT", "COP", "MXN", "ARS", "GBP", "BRL", "CAD", "AUD", "JPY", "CLP"
]
BANCOS_CCI = ["bcp", "interbank", "bbva"]
BANCOS_WALLET = ["yape", "plin"]


class CreateBankAccountSchema(Schema):

    bank_name = fields.String(
        required=True,
        validate=validate.Length(min=1, max=50, error="Nombre de banco inválido")
    )

    account_number = fields.String(
        required=True,
        validate=validate.Length(min=4, max=60, error="Número de cuenta muy corto")
    )

    account_holder = fields.String(
        required=True,
        validate=validate.Length(min=1, error="El nombre del titular es obligatorio")
    )

    account_type = fields.String(
        load_default="savings",
        validate=validate.OneOf(["savings", "checking"])
    )

    currency = fields.String(
        load_default="PEN",
        validate=validate.OneOf(CURRENCIES_SOPORTADAS, error="Moneda no soportada")
    )

    is_primary = fields.Boolean(load_default=False)

    @validates("account_number")
    def validate_account_number(self, value):
        bank = (self.context.get("bank_name") or "").lower() if self.context else ""

        if bank in BANCOS_WALLET:
            if not value.isdigit() or len(value) != 9:
                raise ValidationError("Para Yape/Plin el número debe tener exactamente 9 dígitos")
        elif bank in BANCOS_CCI:
            if not value.isdigit() or len(value) != 20:
                raise ValidationError("El CCI debe tener exactamente 20 dígitos")
