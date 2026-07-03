# 5. Cuentas bancarias y notificaciones

## Cuentas bancarias

- **Debe hacer:** listar, agregar, marcar predeterminada y eliminar cuentas (bloqueando el borrado si estan en uso por una transaccion activa, ya sea como cuenta de comprador o de vendedor).
- **Debe ver:** cuenta predeterminada distinguida, acciones claras (agregar/eliminar/predeterminar).
- **Debe llenar:** banco, numero de cuenta/CCI, moneda, titular.
- **Se espera:** el metodo de pago predeterminado se usa en los flujos de publicar/comprar.

## Notificaciones

- **Debe hacer:** crear notificacion interna, mandar push si hay dispositivo registrado, mostrar contador de no leidas.
- **Debe ver:** diferenciar leidas/no leidas, estado vacio, acciones claras (marcar leida, eliminar).
- **Debe llenar:** nada, solo consulta/acciones.
- **Se espera:** el push llega al telefono real y tambien aparece en el listado interno, sin consumir bateria/datos de mas por sondeos constantes.
