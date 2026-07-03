# 5. Cuentas bancarias y notificaciones

## Cuentas bancarias

- **Debe hacer:** listar, agregar, marcar predeterminada y eliminar cuentas (bloqueando el borrado si estan en uso por una transaccion activa, ya sea como cuenta de comprador o de vendedor).
- **Debe ver:** cuenta predeterminada distinguida, acciones claras (agregar/eliminar/predeterminar).
- **Debe llenar:** banco, numero de cuenta/CCI, moneda, titular.
- **Se espera:** el metodo de pago predeterminado se usa en los flujos de publicar/comprar.

## Notificaciones

- **Debe hacer:** crear notificacion interna (por usuario, solo al destinatario correspondiente) y mandar push si hay dispositivo registrado, **solo en estos casos**: (1) al comprar — se avisa al comprador que su solicitud quedo pendiente y al vendedor que alguien quiere comprarle; (2) al subir un comprobante (voucher), en cualquiera de los dos sentidos (comprador sube → avisa al vendedor; vendedor sube → avisa al comprador); (3) al terminar la operacion (confirmar/liberar) — se avisa al comprador. Ningun otro evento debe generar notificacion (no login, no aprobacion de KYC, no baneos, no disputas, no reclamos).
- **Debe ver:** diferenciar leidas/no leidas, estado vacio, acciones claras (marcar leida, eliminar), contador de no leidas.
- **Debe llenar:** nada, solo consulta/acciones.
- **Se espera:** el push llega al telefono real y tambien aparece en el listado interno, sin consumir bateria/datos de mas por sondeos constantes, y sin ruido de notificaciones para eventos que no le interesan al usuario.
