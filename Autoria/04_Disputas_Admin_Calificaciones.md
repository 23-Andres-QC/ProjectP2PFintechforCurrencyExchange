# 4. Disputas, admin y calificaciones

Disputas del usuario, panel de administracion, calificaciones (ratings), reclamos.

## Disputas del usuario

- **Debe hacer:** dejar que un participante abra disputa sobre una transaccion activa, pasarla a `disputed`, avisar al admin, y evitar una segunda disputa mientras la primera siga abierta o en revision. Si el usuario adjunta una foto de evidencia, debe subirse a almacenamiento real (Supabase) y quedar asociada a la disputa.
- **Debe ver:** motivos claros, explicacion de que un admin revisara, estado de la disputa, selector para adjuntar una foto (opcional) con vista previa de lo que se va a enviar.
- **Debe llenar:** motivo, descripcion, evidencia (foto opcional).
- **Se espera:** la disputa congela el flujo (no se puede confirmar) y aparece para el admin; si se adjunto evidencia, el admin la ve en el detalle de la disputa antes de resolver.

### Nueva logica aplicada: vendedor no recibio el dinero

- **Desde donde:** `Pendientes > Ventas`, dentro de una orden en estado `voucher_uploaded` donde el comprador ya subio voucher/comprobante.
- **Debe hacer:** mostrar al vendedor el boton **"Abrir disputa: no recibi el dinero"** debajo de la accion de liberar fondos. Al tocarlo, la app abre `Registrar Disputa` con la transaccion ya seleccionada.
- **Debe permitir:** que el vendedor abra disputa cuando reviso su cuenta bancaria y el dinero no llego, aun si el comprador adjunto voucher.
- **Debe bloquear:** no debe permitir una segunda disputa abierta para la misma transaccion; si ya existe una disputa en `open` o `under_review`, la app debe mostrar un mensaje claro: "Ya existe una disputa abierta para esta transaccion".
- **Estados permitidos para abrir disputa:** `pending`, `accepted`, `voucher_uploaded`.
- **Estados no permitidos:** `completed`, `closed`, `cancelled`, `disputed`.
- **Se espera:** si el vendedor abre disputa desde Ventas, la transaccion pasa a `disputed`, queda congelada, el admin puede verla en su panel y revisar el voucher/evidencia antes de resolver.

### Nueva logica aplicada: comprador no recibio el dinero del vendedor

- **Desde donde:** detalle de transaccion del comprador, en estado `voucher_uploaded`, cuando el vendedor ya subio su voucher/comprobante de transferencia.
- **Debe hacer:** mostrar al comprador el **voucher del vendedor** para que pueda revisar el comprobante antes de decidir. Si el dinero no llego a su cuenta, el comprador puede tocar **"Abrir Disputa (no recibi el dinero)"**.
- **Debe permitir:** que el comprador abra disputa aunque exista un voucher del vendedor, porque el voucher puede estar equivocado, no corresponder al monto/cuenta, o la transferencia puede no haberse reflejado.
- **Debe ver en detalle de disputa:** voucher del comprador, voucher del vendedor, evidencia adjunta, motivo, descripcion, estado de la transaccion y resolucion del admin.
- **Se espera:** el admin tenga ambos comprobantes visibles antes de resolver y el usuario pueda demostrar con evidencia adicional que el dinero no llego.

## Panel de administracion

- **Debe hacer:** exigir rol admin en todo, dejar ver dashboard/usuarios/disputas/reclamos, resolver disputas con nota obligatoria, banear usuarios cortando su sesion, dejar rastro auditable de cada accion. El acceso al panel es una **pestana propia en la barra inferior** (visible solo para admin, junto a Mercado y Perfil), no una opcion escondida dentro del menu de Perfil.
- **Debe ver:** tarjeta de resumen compacta (una sola fila: nombre + volumen/disputas/usuarios, sin ocupar media pantalla), info suficiente antes de resolver (comprador, vendedor, comprobantes, montos, timeline, evidencia adjunta si el usuario subio una), confirmacion antes de banear/resolver. Al tocar "Favor Comprador"/"Favor Vendedor" debe abrir un dialogo pidiendo la nota de resolucion antes de mandar la peticion — si se manda sin nota el backend la rechaza, asi que la app nunca debe intentar resolver sin haberla pedido primero.
- **Debe llenar:** nota de resolucion (disputas y reclamos, obligatoria en ambas), decision (a favor de comprador/vendedor).
- **Se espera:** admin resuelve sin romper saldos, queda registro de lo que hizo, y un usuario baneado pierde el acceso de verdad.

### Que significa resolver una disputa (no importa quien la abrio, comprador o vendedor)

- **A favor del comprador:** significa que el comprador tiene razon en que el dinero/voucher del vendedor no es suficiente o no llego. La transaccion **no se completa sola**; vuelve a `voucher_uploaded` y se notifica al vendedor para que suba el voucher correcto antes de liberar fondos.
- **A favor del vendedor:** significa que el vendedor tiene razon o que el comprobante/proceso aun necesita tiempo para reflejarse. La transaccion vuelve a `voucher_uploaded` y se notifica al comprador que debe esperar un poco mas de tiempo.
- **No debe hacer:** cancelar automaticamente la transaccion al resolver una disputa a favor del vendedor. Tampoco debe completar automaticamente la transaccion al resolver a favor del comprador.
- **Se espera:** la resolucion del admin guie el siguiente paso mediante notificaciones: vendedor corrige voucher si gana el comprador; comprador espera si gana el vendedor.

## Calificaciones (ratings)

- **Debe hacer:** dejar calificar solo transacciones completadas, solo a los participantes reales, sin duplicados.
- **Debe ver:** estrellas, comentario opcional, confirmacion de exito.
- **Debe llenar:** puntaje (estrellas), comentario opcional.
- **Se espera:** la reputacion no se puede inflar con calificaciones invalidas.

## Reclamos

- **Debe hacer:** guardar el reclamo, dejar que el admin lo resuelva con nota.
- **Debe ver:** confirmacion al enviar el reclamo.
- **Debe llenar:** categoria, mensaje/descripcion.
- **Se espera:** todo reclamo puede ser resuelto por un admin.
