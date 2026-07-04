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

## Panel de administracion

- **Debe hacer:** exigir rol admin en todo, dejar ver dashboard/usuarios/disputas/reclamos, resolver disputas con nota obligatoria, banear usuarios cortando su sesion, dejar rastro auditable de cada accion. El acceso al panel es una **pestana propia en la barra inferior** (visible solo para admin, junto a Mercado y Perfil), no una opcion escondida dentro del menu de Perfil.
- **Debe ver:** tarjeta de resumen compacta (una sola fila: nombre + volumen/disputas/usuarios, sin ocupar media pantalla), info suficiente antes de resolver (comprador, vendedor, comprobantes, montos, timeline, evidencia adjunta si el usuario subio una), confirmacion antes de banear/resolver. Al tocar "Favor Comprador"/"Favor Vendedor" debe abrir un dialogo pidiendo la nota de resolucion antes de mandar la peticion — si se manda sin nota el backend la rechaza, asi que la app nunca debe intentar resolver sin haberla pedido primero.
- **Debe llenar:** nota de resolucion (disputas y reclamos, obligatoria en ambas), decision (a favor de comprador/vendedor).
- **Se espera:** admin resuelve sin romper saldos, queda registro de lo que hizo, y un usuario baneado pierde el acceso de verdad.

### Que significa resolver una disputa (no importa quien la abrio, comprador o vendedor)

- **A favor del comprador:** significa que el comprador cumplio. La transaccion **no se completa sola** — vuelve al estado "comprobante subido" y se le avisa al vendedor con urgencia que debe liberar el pago ya (usando el flujo normal de confirmar, que es el que genera el recibo). El objetivo es presionar al vendedor a terminar, no forzar el cierre.
- **A favor del vendedor:** significa que el comprobante del comprador no fue valido.
  - **Primera vez** que pasa esto en esa transaccion: no se cancela. Se le da al comprador una **segunda oportunidad**: la transaccion vuelve al estado "aceptada" para que pueda subir el comprobante correcto, y se le notifica con advertencia.
  - **Segunda vez** que se resuelve a favor del vendedor en la misma transaccion (el comprador fallo otra vez): recien ahi se **cancela de verdad** y se restaura el saldo disponible de la oferta.
- Verificado con curl contra el backend real: favor comprador -> `voucher_uploaded`; favor vendedor 1ra vez -> `accepted` (oferta sin restaurar); favor vendedor 2da vez -> `cancelled` (oferta restaurada correctamente).

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
