# 3. Transacciones

Crear transaccion, aceptar orden, voucher comprador, voucher vendedor/confirmar, cancelar/pausar/cerrar, recibo e historial.

Estados: `pending` -> `accepted` -> `voucher_uploaded` -> `completed`. En cualquier punto antes de completarse puede pasar a `disputed`. Tambien existen `cancelled`, `paused`, `closed`.

## Crear transaccion

- **Debe hacer:** validar oferta/limites/disponibilidad/KYC del comprador, calcular el monto final, evitar una segunda compra activa duplicada sobre la misma oferta, crear la transaccion `pending`, descontar disponibilidad, avisar al vendedor.
- **Debe ver:** confirmacion antes de comprar, monto a pagar, moneda, vendedor, tasa.
- **Debe llenar:** monto a comprar.
- **Se espera:** una compra crea una sola transaccion y descuenta el monto exacto.

## Aceptar orden (vendedor)

- **Debe hacer:** dejar que solo el vendedor dueno acepte, solo desde estado `pending`, pasar a `accepted`, avisar al comprador.
- **Debe ver:** boton de aceptar claro, dialogo de confirmacion, estado "esperando pago del comprador".
- **Debe llenar:** nada (solo confirmar).
- **Se espera:** vendedor acepta y comprador ve el cambio de estado sin error.

## Voucher del comprador

- **Debe hacer:** aceptar el comprobante de pago del comprador solo si la transaccion esta `pending` o `accepted`, y pasar a `voucher_uploaded`.
- **Debe ver:** datos bancarios del vendedor, preview del comprobante, mensaje "esperando confirmacion del vendedor".
- **Debe llenar:** foto/comprobante de la transferencia.
- **Se espera:** el voucher aparece en la vista del vendedor y cambia el estado; no se puede subir sobre una transaccion cancelada, en disputa o ya completada.

## Voucher del vendedor y confirmar/liberar

- **Debe hacer:** aceptar el comprobante del vendedor; al confirmar, exigir ambos comprobantes, marcar `completed`, generar recibo PDF, avisar al comprador.
- **Debe ver:** ambos comprobantes, resumen final antes de confirmar, boton de liberar marcado como irreversible con confirmacion.
- **Debe llenar:** foto/comprobante propio del vendedor.
- **Se espera:** solo se completa con ambos vouchers, con el vendedor autorizado, y nunca si hay una disputa abierta.

## Cancelar / pausar / cerrar

- **Debe hacer:** cancelar o pausar solo desde un estado que no sea `completed` ni `closed`, restaurando el monto disponible de la oferta cuando corresponda.
- **Debe ver:** accion destructiva con confirmacion, estado actualizado visible.
- **Debe llenar:** nada (solo confirmar / motivo si aplica).
- **Se espera:** cancelar restaura correctamente el saldo de la oferta; no se puede cancelar/pausar una transaccion ya completada o cerrada.

## Recibo e historial

- **Debe hacer:** generar el PDF al completar (sin depender de que el correo funcione), mostrar historial y detalle solo al usuario dueno/participante.
- **Debe ver:** badges de estado, fechas legibles, montos claros con moneda, recibo descargable.
- **Debe llenar:** nada, solo consulta.
- **Se espera:** historial refleja datos reales y abre el detalle correcto.
