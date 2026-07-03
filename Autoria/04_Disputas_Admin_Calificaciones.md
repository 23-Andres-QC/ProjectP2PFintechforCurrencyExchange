# 4. Disputas, admin y calificaciones

Disputas del usuario, panel de administracion, calificaciones (ratings), reclamos.

## Disputas del usuario

- **Debe hacer:** dejar que un participante abra disputa sobre una transaccion activa, pasarla a `disputed`, avisar al admin, y evitar una segunda disputa mientras la primera siga abierta o en revision.
- **Debe ver:** motivos claros, explicacion de que un admin revisara, estado de la disputa.
- **Debe llenar:** motivo, descripcion.
- **Se espera:** la disputa congela el flujo (no se puede confirmar) y aparece para el admin.

## Panel de administracion

- **Debe hacer:** exigir rol admin en todo, dejar ver dashboard/usuarios/disputas/reclamos, resolver disputas con nota obligatoria restaurando saldos correctamente, banear usuarios cortando su sesion, dejar rastro auditable de cada accion.
- **Debe ver:** info suficiente antes de resolver (comprador, vendedor, comprobantes, montos, timeline), confirmacion antes de banear/resolver.
- **Debe llenar:** nota de resolucion (disputas y reclamos), decision (a favor de comprador/vendedor).
- **Se espera:** admin resuelve sin romper saldos, queda registro de lo que hizo, y un usuario baneado pierde el acceso de verdad.

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
