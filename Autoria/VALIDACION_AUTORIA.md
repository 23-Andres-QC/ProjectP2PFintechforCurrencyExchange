# Validacion de Autoria

Fecha: 2026-07-04

## Resultado general

Validacion ejecutada sobre los seis documentos de `Autoria` contra backend Flask y app Android.

- Backend: sin errores de sintaxis en `app` y `migrations` con `python -m compileall app migrations`.
- Android: pruebas unitarias y compilacion de debug unit test correctas con `.\gradlew.bat testDebugUnitTest`.
- Limitacion: no se pudo levantar Flask localmente porque el entorno actual no tiene instaladas las dependencias Python (`flask`, `flask_sqlalchemy`, etc.).

## Correcciones aplicadas

- Registro: backend ahora exige nombre, password minimo de 8 caracteres, DNI de 8 digitos si se envia, terminos aceptados y bloquea creacion publica de admins.
- Cuentas bancarias: backend normaliza datos y marca automaticamente como principal la primera cuenta del usuario.
- Publicar oferta: backend valida que la cuenta de pago este registrada/configurada en la app para el vendedor y coincida con la moneda fiat recibida. No verifica contra un banco real.
- Crear transaccion: backend exige monto mayor a 0 y toma la cuenta destino desde la oferta, no desde el cliente.
- Transacciones duplicadas: una compra en `disputed` tambien bloquea compras duplicadas activas sobre la misma oferta.
- Calificar/cerrar: al calificar como comprador, el backend cierra la transaccion si estaba `completed`.
- Admin Android: la pantalla Admin ahora pertenece a las rutas con barra inferior para que la pestana admin sea visible cuando corresponde.
- Pendientes Android: el contador de compras incluye operaciones `completed` pendientes de cierre.
- Recuperar password: el mensaje ya no revela si el correo existe.
- Detalle de transaccion: se retiro el estado OCR fijo porque no hay verificacion OCR real implementada.
- Ban de admin: los JWT de usuarios inactivos o baneados quedan rechazados en rutas protegidas.

## Validacion por documento

### 01 Autenticacion y Perfil

Estado: validado con ajustes.

- Sesion guardada lleva a Mercado desde `AuthGate`.
- Login guarda token, registra FCM de forma best-effort y no bloquea si falla.
- Registro valida terminos en Android y ahora tambien refuerza reglas en backend.
- KYC sube documentos y queda aprobado automaticamente.
- Recuperar password usa Firebase y ahora muestra resultado neutral.
- Perfil consulta datos del servidor y logout limpia sesion local.

### 02 Mercado y Ofertas

Estado: validado con ajustes.

- Mercado excluye ofertas propias en backend y en Android.
- Tasas se consultan desde API de exchange.
- Publicar oferta exige KYC, cuenta bancaria y limites coherentes.
- La cuenta usada para recibir pago ahora se valida contra cuentas registradas del vendedor dentro de la demo.
- Mis ofertas incluye filtro de finalizadas.

### 03 Transacciones

Estado: validado con ajustes.

- Crear transaccion valida oferta, KYC, limites, disponibilidad y duplicados.
- El comprador no necesita cuenta bancaria real para comprar.
- El vendedor ve pendientes y acepta/cancela desde estados permitidos.
- Voucher de comprador y voucher de vendedor estan separados.
- Confirmar exige ambos comprobantes y genera recibo PDF.
- Calificar desde comprador ahora tambien cierra backend a `closed`.

### 04 Disputas, Admin y Calificaciones

Estado: validado.

- Disputas solo para participantes y estados permitidos.
- Evidencia opcional se sube por storage.
- No se permite segunda disputa abierta/en revision.
- Admin exige rol en backend y resuelve con nota obligatoria.
- Usuarios baneados quedan sin acceso a rutas protegidas aunque conserven un token anterior.
- Resolucion devuelve la transaccion a `voucher_uploaded` sin completar/cancelar automaticamente.
- Ratings evitan duplicados y recalculan reputacion.

### 05 Cuentas Bancarias y Notificaciones

Estado: validado con ajustes.

- Crear cuenta valida banco, titular, numero y moneda.
- Primera cuenta ahora queda principal tambien si el cliente no manda `is_primary`.
- Borrado se bloquea si hay transaccion activa usando esa cuenta.
- Notificaciones internas y push estan implementadas para eventos transaccionales principales.

### 06 Soporte, Legales y Chatbot

Estado: validado con ajustes.

- Reclamos validan tipo permitido y descripcion obligatoria.
- Admin resuelve reclamos con nota.
- Chatbot esta limitado por prompt a funciones reales y evita prometer OCR, custodia bancaria, liberacion automatica o soporte 24/7.
- Se retiro una promesa visual de OCR en detalle de transaccion.
