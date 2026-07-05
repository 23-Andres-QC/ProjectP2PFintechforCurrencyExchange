# 5. Cuentas bancarias y notificaciones

## Cuentas bancarias

- **Debe hacer:** listar, agregar, marcar predeterminada y eliminar cuentas (bloqueando el borrado si estan en uso por una transaccion activa, ya sea como cuenta de comprador o de vendedor).
- **Debe ver:** cuenta predeterminada distinguida, acciones claras (agregar/eliminar/predeterminar).
- **Debe llenar:** banco, numero de cuenta/CCI, moneda, titular.
- **Se espera:** el metodo de pago predeterminado se usa en los flujos de publicar/comprar.

### Validacion aplicada: crear cuenta/tarjeta bancaria

- **Debe pedir al crear:** banco, titular, numero de cuenta/CCI o wallet, y moneda.
- **Debe validar:** Yape/Plin aceptan exactamente 9 digitos; BCP/Interbank/BBVA aceptan CCI de 20 digitos; cuentas internacionales/wallets aceptan entre 4 y 60 caracteres.
- **Debe bloquear:** no se puede crear si falta el titular o si el numero no cumple la regla del banco seleccionado.
- **Se espera:** al crear correctamente, la cuenta aparece en la lista; si es la primera cuenta, queda como principal; si el backend rechaza por duplicado o formato invalido, la app muestra un mensaje claro.

### Validacion aplicada: mercado, publicacion y transaccion

- **Publicar oferta:** el vendedor debe elegir una cuenta bancaria de la moneda fiat en la que recibira el pago; esa cuenta queda guardada en `payment_methods`.
- **Comprar en mercado:** la cuenta del comprador no es obligatoria; la pantalla muestra solo la cuenta o numero configurado por el vendedor dentro de la oferta.
- **Crear transaccion:** `vendor_payment_account` se llena con la cuenta configurada en la oferta y ya no usa un banco fijo de respaldo.
- **Ver transaccion:** el resumen de pago muestra la cuenta destino completa guardada desde la oferta.

## Notificaciones

- **Debe hacer:** crear notificacion interna (por usuario, solo al destinatario correspondiente) y mandar push si hay dispositivo registrado, **solo en estos casos**: (1) al comprar — se avisa al comprador que su solicitud quedo pendiente y al vendedor que alguien quiere comprarle; (2) al subir un comprobante (voucher), en cualquiera de los dos sentidos (comprador sube → avisa al vendedor; vendedor sube → avisa al comprador); (3) al terminar la operacion (confirmar/liberar) — se avisa al comprador. Ningun otro evento debe generar notificacion (no login, no aprobacion de KYC, no baneos, no disputas, no reclamos).
- **Debe ver:** diferenciar leidas/no leidas, estado vacio, acciones claras (marcar leida, eliminar), contador de no leidas.
- **Debe llenar:** nada, solo consulta/acciones.
- **Se espera:** el push llega al telefono real y tambien aparece en el listado interno, sin consumir bateria/datos de mas por sondeos constantes, y sin ruido de notificaciones para eventos que no le interesan al usuario.
