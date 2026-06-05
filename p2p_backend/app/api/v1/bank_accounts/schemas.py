from marshmallow import Schema, fields, validate, validates, ValidationError

# Lista de bancos permitidos en la plataforma
BANCOS_PERMITIDOS = ["bcp", "interbank", "bbva", "yape", "plin"]

class CreateBankAccountSchema(Schema):
    """
    Valida los datos cuando el usuario quiere agregar una cuenta bancaria
    """

    # El banco debe ser uno de los permitidos
    bank_name = fields.String(
        required=True,
        validate=validate.OneOf(
            BANCOS_PERMITIDOS,
            error="Banco no válido. Debe ser: bcp, interbank, bbva, yape o plin"
        )
    )

    # El número de cuenta es obligatorio
    account_number = fields.String(
        required=True,
        validate=validate.Length(min=1, error="El número de cuenta es obligatorio")
    )

    # El titular de la cuenta es obligatorio
    account_holder = fields.String(
        required=True,
        validate=validate.Length(min=1, error="El nombre del titular es obligatorio")
    )

    # El tipo de cuenta (ahorros o corriente)
    account_type = fields.String(
        load_default="savings",
        validate=validate.OneOf(["savings", "checking"])
    )

    # La moneda (por defecto soles)
    currency = fields.String(
        load_default="PEN",
        validate=validate.OneOf(["PEN", "USD", "EUR"])
    )

    # Si es la cuenta principal
    is_primary = fields.Boolean(load_default=False)


    @validates("account_number")
    def validate_account_number(self, value):
        if not value.isdigit():
            raise ValidationError("El número solo debe contener dígitos")
    
        bank = self.context.get("bank_name") if self.context else None

        if bank in ["yape", "plin"]:
            if len(value) != 9:
             raise ValidationError("Para Yape y Plin el número debe tener 9 dígitos")
        elif bank in ["bcp", "interbank", "bbva"]:
            if len(value) != 20:
                raise ValidationError("El CCI debe tener exactamente 20 dígitos")