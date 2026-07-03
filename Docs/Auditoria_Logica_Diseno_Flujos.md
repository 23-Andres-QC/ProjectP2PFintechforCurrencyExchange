# Auditoria de logica, flujos y diseno - PeruExchange

Fecha de revision: 2026-07-02  
Alcance revisado: app Android `p2p_android` y backend Flask `p2p_backend`.

## Resumen ejecutivo

El sistema ya tiene una base funcional amplia: onboarding, login, registro con KYC, mercado, publicacion de ofertas, transacciones, comprobantes, disputas, calificaciones, notificaciones, cuentas bancarias, perfil, soporte y administracion. La arquitectura general esta clara: Android consume una API REST `/api/v1`, el backend concentra reglas de negocio en servicios y la app usa ViewModels + repositorios.

Los mayores riesgos no estan en que falten pantallas, sino en mantener contratos y reglas de negocio alineados entre Android y backend. El flujo de transacciones ya contempla `accepted` en backend y Android; KYC queda en revision al subir documentos y solo se verifica desde administracion.

En diseno, la app tiene identidad visual fuerte, pero varias pantallas dependen de ajustes manuales de espaciado, offsets y tamanos. Eso puede verse bien en un emulador especifico y romperse en otros tamanos, especialmente registro, header azul, stepper, carrusel de tasas y formularios largos.


## Auditoria secuencial por pantallas y flujos

Esta seccion es el recorrido ordenado que se debe seguir para evaluar la app como si fuera una revision completa de producto. En cada punto se documenta la pantalla, el flujo que debe seguir, lo que se debe revisar, los errores/riesgos encontrados y el criterio para decir que esa parte funciona.

### 1. Pantalla Welcome / Inicio

Pantallas a revisar:

- `WelcomeScreen`
- Ruta `welcome`

Flujo esperado:

1. Usuario abre la app.
2. Ve dos acciones claras: `Iniciar sesion` y `Registrate`.
3. Si toca iniciar sesion, navega a `login`.
4. Si toca registrarse, navega a `register`.
5. Si ya tiene sesion valida, `AuthGate` lo envia directo a Mercado.

Evaluacion de logica:

- Verificar token guardado en `AuthGate` antes de mostrar Welcome.
- Verificar que no se pueda acceder a Market sin sesion valida.
- Verificar que cerrar sesion limpie token y vuelva a Welcome.

Evaluacion de diseno:

- Logo visible y centrado.
- Botones principales claros, sin competir entre si.
- El diseno debe verse bien en telefono pequeno y grande.

Criterio de aprobado:

- Usuario nuevo entiende que puede iniciar sesion o registrarse.
- Usuario con sesion valida entra directo al sistema.
- Usuario sin sesion no puede entrar a pantallas protegidas.

### 2. Login

Pantallas a revisar:

- `LoginScreen`
- `LoginViewModel`
- `AuthRepositoryImpl`
- API backend `POST /auth/login`
- Registro FCM `POST /notifications/fcm-token`

Flujo esperado:

1. Usuario escribe correo y contrasena.
2. App valida campos vacios/formato.
3. App llama backend login.
4. Backend retorna access token, refresh token, usuario y rol.
5. App guarda sesion.
6. App registra token FCM.
7. Navega a `market`.

Evaluacion de logica:

- Probar credenciales correctas.
- Probar password incorrecto.
- Probar usuario inexistente.
- Probar usuario baneado/inactivo si existe esa regla.
- Verificar que token y rol quedan guardados.
- Verificar que si FCM falla, login no se rompe pero se registra el error.

Evaluacion de diseno:

- Header azul con logo consistente.
- Password con opcion de mostrar/ocultar.
- Mensajes de error debajo o cerca del campo.
- Boton deshabilitado o con loading mientras inicia sesion.

Errores/riesgos encontrados:

- FCM puede no llegar si permiso Android esta denegado o si el usuario nunca actualiza token.
- Tokens locales no estan documentados como cifrados.

Criterio de aprobado:

- Login correcto entra a Market.
- Login incorrecto muestra error claro.
- FCM queda registrado para el usuario logueado.

### 3. Registro de cuenta

Pantallas a revisar:

- `RegisterScreen`
- `RegisterViewModel`
- `AuthBrandHeader`
- API backend `POST /auth/register`
- Link TermsFeed de terminos

Flujo esperado:

1. Usuario entra desde Welcome o Login.
2. Completa datos personales.
3. Completa KYC/documentos.
4. Completa contrato/firma si aplica.
5. Crea contrasena.
6. Marca obligatoriamente terminos y condiciones.
7. App crea cuenta.
8. App registra FCM.
9. Navega a Market o a estado de KYC pendiente segun la regla final.

Evaluacion de logica:

- Verificar validacion de nombre, email, DNI, contrasena y confirmacion.
- Verificar que los pasos no permitan avanzar con datos incompletos.
- Verificar que los numeros del stepper solo permitan volver a pasos ya alcanzados.
- Verificar que checkbox de terminos sea obligatorio.
- Verificar que el backend guarde aceptacion de terminos con fecha, URL y version.
- Verificar si despues de registro el usuario debe quedar activo o pendiente de KYC.

Evaluacion de diseno:

- Header azul mas compacto que login, sin tapar el stepper.
- Stepper justo debajo del azul, sin contenedor blanco si ese es el diseno final.
- Formulario completo visible con scroll natural.
- No debe quedar cortado el boton continuar en pantallas pequenas.

Errores/riesgos encontrados:

- Registro y KYC estan mezclados en un flujo largo; si algo falla al final, el usuario puede perder contexto.
- El header/stepper tiene riesgo visual por ajustes manuales.

Criterio de aprobado:

- No se crea cuenta sin aceptar terminos.
- La aceptacion queda guardada en backend.
- Registro completo funciona en telefono real sin superposiciones.

### 4. KYC y firma

Pantallas a revisar:

- Pasos KYC dentro de `RegisterScreen`
- `KycScreen`
- `KycSummaryScreen`
- `KycViewModel`
- API backend `POST /users/kyc`
- API backend `POST /users/signature`

Flujo esperado:

1. Usuario sube DNI frontal.
2. Usuario sube DNI reverso.
3. Usuario sube selfie.
4. Usuario agrega firma si aplica.
5. App envia multipart al backend.
6. Backend guarda documentos.
7. Backend deja KYC en revision.
8. Admin o proveedor aprueba/rechaza.

Evaluacion de logica:

- Verificar nombres multipart: backend espera `dni_front`, `dni_back`, `selfie`, `signature`.
- Verificar compresion y peso de imagenes.
- Verificar respuesta del backend.
- Verificar que KYC no se apruebe automaticamente.
- Verificar que usuario no pueda publicar hasta KYC aprobado.

Evaluacion de diseno:

- Fotos deben mostrar estado: pendiente, cargada, error.
- Debe existir forma de reemplazar foto.
- Mensajes deben explicar si esta en revision, aprobado o rechazado.

Criterio de aprobado:

- Subir documentos no equivale a aprobacion.
- Usuario ve estado real de revision.
- Admin puede aprobar/rechazar o existe proveedor KYC.

### 5. Recuperar contrasena

Pantallas a revisar:

- `ForgotPasswordScreen`
- Backend de recuperacion si existe

Flujo esperado:

1. Usuario entra desde Login.
2. Escribe correo.
3. Backend genera token de recuperacion.
4. Usuario recibe email.
5. Usuario cambia contrasena.

Evaluacion de logica:

- Confirmar si existe endpoint real.
- Confirmar expiracion de token.
- Confirmar limite de intentos.

Evaluacion de diseno:

- Mensaje neutral: no revelar si el correo existe o no.
- Confirmacion clara de siguiente paso.

Errores/riesgos encontrados:

- No se confirmo en rutas revisadas un flujo backend completo para recuperar password.

Criterio de aprobado:

- Usuario puede recuperar acceso sin intervencion manual.

### 6. Mercado principal

Pantallas a revisar:

- `MarketScreen`
- `MarketViewModel`
- `ExchangeRateMarquee`
- `FilterDropdown`
- APIs `GET /offers`, `GET /exchange/rates`, `GET /exchange/currencies`

Flujo esperado:

1. Usuario entra a Market.
2. App carga ofertas activas.
3. App excluye ofertas propias.
4. App carga tasas de cambio reales.
5. Carrusel muestra pares respecto al sol peruano.
6. Usuario filtra moneda origen/destino.
7. Usuario selecciona oferta o usa matching automatico.

Evaluacion de logica:

- Verificar que las ofertas vengan del backend.
- Verificar que no se pueda comprar oferta propia.
- Verificar que las tasas vengan de `GET /exchange/ticker?quote=PEN`.
- Verificar que todos los pares del carrusel sean contra PEN.
- Verificar que matching final venga de backend.

Evaluacion de diseno:

- Carrusel infinito sin espacios blancos ni reinicio visible.
- Chips de tasas no deben cortarse.
- Menus de moneda muestran 5 opciones visibles y scroll para las demas.
- No agregar buscador si la decision de producto es no tenerlo.
- Estado sin conexion debe ser claro y tener reintentar.

Errores/riesgos encontrados:

- Carrusel debe revisarse visualmente para confirmar que no deje espacios blancos ni reinicio visible.
- Revisar que los criterios de matching backend sigan alineados con producto.

Criterio de aprobado:

- Market carga rapido.
- Carrusel muestra todos los tipos respecto a PEN.
- Filtros funcionan sin tapar toda la pantalla.
- Comprar/match lleva a una transaccion valida.

### 7. Publicar oferta

Pantallas a revisar:

- `PublishScreen`
- `PublishViewModel`
- API `POST /offers`
- API `GET /bank-accounts`

Flujo esperado:

1. Vendedor entra a Publicar.
2. Selecciona moneda, monto, tasa, limites y metodo de pago.
3. App valida datos.
4. Backend valida permisos y KYC.
5. Backend crea oferta activa.
6. Oferta aparece en Market para otros usuarios.

Evaluacion de logica:

- Validar rol vendedor.
- Validar KYC aprobado.
- Validar min/max.
- Validar que `amount`, `price_per_unit` sean positivos.
- Validar que tenga cuenta bancaria/metodo de pago.

Evaluacion de diseno:

- Debe quedar claro que moneda vende y que moneda recibe.
- La tasa debe estar explicada con ejemplo.
- Errores deben aparecer antes de enviar.

Errores/riesgos encontrados:

- Pendiente: exigir cuenta bancaria/metodo de pago antes de publicar si el producto lo define como obligatorio.

Criterio de aprobado:

- Solo usuario autorizado y verificado publica.
- Oferta creada se ve en Market de otro usuario.

### 8. Mis ofertas

Pantallas a revisar:

- `MyOffersScreen`
- `MyOffersViewModel`
- APIs `GET /offers/my-offers`, `PATCH /offers/{id}`, `DELETE /offers/{id}`

Flujo esperado:

1. Vendedor ve sus ofertas.
2. Puede cerrar/cancelar.
3. Puede editar datos permitidos.
4. Cambios se reflejan en Market.

Evaluacion de logica:

- No permitir editar oferta de otro usuario.
- No permitir modificar monto disponible si hay transacciones activas.
- Cancelar oferta no debe afectar transacciones ya creadas.

Evaluacion de diseno:

- Estado de oferta visible: activa, pausada, cerrada.
- Mostrar disponible vs monto original.
- Accion destructiva con confirmacion.

Errores/riesgos encontrados:

- Revisar que futuras ediciones no vuelvan a exponer `available_amount` directamente.

Criterio de aprobado:

- Gestionar oferta no rompe transacciones existentes.

### 9. Iniciar compra / crear transaccion

Pantallas a revisar:

- `MarketScreen`
- `TransactionScreen`
- `TransactionViewModel`
- API `POST /transactions`

Flujo esperado:

1. Comprador elige oferta.
2. Define monto.
3. Backend valida oferta, min/max y disponibilidad.
4. Backend crea transaccion `pending`.
5. Backend descuenta disponibilidad.
6. Vendedor recibe notificacion.
7. Comprador pasa a pantalla de transaccion.

Evaluacion de logica:

- Backend no debe aceptar compra de oferta propia.
- Backend debe calcular `amount_to`.
- Doble tap no debe crear dos transacciones.
- Si falla creacion, no debe descontar oferta.

Evaluacion de diseno:

- Confirmacion antes de comprar.
- Mostrar monto a pagar, moneda, vendedor y tasa.
- Estado `pendiente de vendedor` claro.

Errores/riesgos encontrados:

- Falta idempotencia.

Criterio de aprobado:

- Una compra crea una sola transaccion y descuenta monto exacto.

### 10. Pendientes del vendedor / aceptar orden

Pantallas a revisar:

- `PendingScreen`
- `VendorInboxScreen`
- `TransactionViewModel.acceptTransaction`
- API `PATCH /transactions/{id}/status`

Flujo esperado:

1. Vendedor ve orden pendiente.
2. Vendedor revisa comprador, monto y cuenta.
3. Vendedor acepta.
4. Backend cambia estado a `accepted`.
5. Comprador recibe notificacion para pagar.

Evaluacion de logica:

- Solo vendedor de esa transaccion puede aceptar.
- Aceptar desde estado distinto a `pending` debe fallar.
- Debe registrar `accepted_at`.
- Cancelar desde `accepted` debe restaurar oferta.

Evaluacion de diseno:

- CTA aceptar claro.
- Dialogo de confirmacion.
- Estado posterior visible: esperando pago del comprador.

Criterio de aprobado:

- Vendedor acepta y comprador ve cambio de estado sin error.

### 11. Pago del comprador y subida de voucher

Pantallas a revisar:

- `TransactionScreen`
- `TransactionViewModel.uploadVoucher`
- API `POST /transactions/{id}/voucher`

Flujo esperado:

1. Comprador ve datos bancarios del vendedor.
2. Realiza transferencia externa.
3. Sube comprobante.
4. Backend guarda imagen.
5. Estado pasa a `voucher_uploaded`.
6. Vendedor recibe notificacion.

Evaluacion de logica:

- Solo comprador o participante valido puede subir.
- Imagen debe validarse por peso/formato.
- No debe aceptar voucher en transaccion cancelada/disputada/completada.

Evaluacion de diseno:

- Datos bancarios claros y copiables.
- Preview del comprobante.
- Estado de carga y error.
- Mensaje claro: "esperando confirmacion del vendedor".

Errores/riesgos encontrados:

- No hay validacion real de comprobante/OCR aunque algunas pantallas/textos pueden sugerirlo.

Criterio de aprobado:

- Voucher subido aparece en vista del vendedor y cambia estado.

### 12. Voucher del vendedor y confirmar/liberar

Pantallas a revisar:

- `VendorInboxScreen`
- `TransactionViewModel.uploadVendorVoucher`
- `TransactionViewModel.confirmTransaction`
- APIs `POST /transactions/{id}/vendor-voucher`, `POST /transactions/{id}/confirm`

Flujo esperado:

1. Vendedor revisa voucher del comprador.
2. Vendedor sube su comprobante.
3. Vendedor confirma/libera.
4. Backend exige ambos vouchers.
5. Backend marca `completed`.
6. Backend genera recibo PDF.
7. Comprador recibe notificacion y recibo.

Evaluacion de logica:

- Confirmar solo desde `voucher_uploaded`.
- Confirmar solo si el vendedor es el duenio de la oferta.
- Confirmar debe estar bloqueado si hay disputa.
- El recibo debe existir aunque falle el email.

Evaluacion de diseno:

- Boton de liberar debe ser irreversible y tener confirmacion.
- Mostrar resumen final antes de confirmar.
- Mostrar ambos comprobantes.

Errores/riesgos encontrados:

- No hay verificacion bancaria real.
- Se debe revisar bloqueo de confirmacion cuando hay disputa.

Criterio de aprobado:

- Transaccion solo completa con ambos vouchers y vendedor autorizado.

### 13. Recibo, historial y detalle

Pantallas a revisar:

- `ReceiptScreen`
- `HistoryScreen`
- `TransactionDetailScreen`
- APIs `GET /transactions`, `GET /transactions/{id}`

Flujo esperado:

1. Usuario ve historial.
2. Filtra pendientes/completadas si aplica.
3. Abre detalle.
4. Si esta completada, puede ver recibo.
5. Puede calificar o reportar segun estado.

Evaluacion de logica:

- Historial debe mostrar solo transacciones del usuario.
- Recibo solo para participantes.
- Estados deben mapearse igual que backend.

Evaluacion de diseno:

- Badges de estado consistentes.
- Fechas legibles.
- Montos claros con moneda.

Errores/riesgos encontrados:

- Revisar que todos los filtros de historial incluyan los mismos estados que backend.

Criterio de aprobado:

- Historial refleja datos reales y abre detalle correcto.

### 14. Calificacion y reviews

Pantallas a revisar:

- `RatingScreen`
- `ReviewsScreen`
- APIs `POST /ratings`, `GET /ratings/received`

Flujo esperado:

1. Usuario termina transaccion.
2. App ofrece calificar.
3. Backend valida que usuario participo.
4. Backend evita rating duplicado.
5. Rating aparece en perfil/reviews.

Evaluacion de logica:

- Solo transacciones completadas.
- Solo participantes.
- Un rating por transaccion/usuario.

Evaluacion de diseno:

- Estrellas claras.
- Comentario opcional.
- Exito y salto a pantalla correcta.

Errores/riesgos encontrados:

- Se debe confirmar validacion backend contra duplicados y estado completado.

Criterio de aprobado:

- Reputacion no se puede inflar con ratings invalidos.

### 15. Disputas del usuario

Pantallas a revisar:

- `RegisterDisputeScreen`
- `MyDisputesScreen`
- `DisputeDetailScreen`
- APIs `POST /transactions/{id}/dispute`, `GET /disputes/my-disputes`, `GET /disputes/{id}`

Flujo esperado:

1. Usuario abre disputa desde transaccion activa.
2. Selecciona motivo y descripcion.
3. Backend valida participante y estado.
4. Backend cambia transaccion a `disputed`.
5. Usuario ve disputa en lista.
6. Admin la revisa.

Evaluacion de logica:

- No permitir disputa en completada/cancelada.
- No permitir doble disputa.
- Bloquear confirmacion mientras esta disputada.

Evaluacion de diseno:

- Motivos claros.
- Explicar que un admin revisara.
- Mostrar estado de disputa.

Errores/riesgos encontrados:

- Disputa no contempla adjuntos/evidencias adicionales.

Criterio de aprobado:

- Disputa congela flujo y aparece para admin.

### 16. Admin

Pantallas a revisar:

- `AdminScreen`
- `AdminViewModel`
- APIs `/admin/dashboard`, `/admin/users`, `/admin/disputes`, `/admin/complaints`

Flujo esperado:

1. Admin entra desde perfil o ruta admin.
2. Ve dashboard.
3. Revisa usuarios.
4. Revisa disputas.
5. Toma disputa.
6. Resuelve a favor de comprador o vendedor.
7. Revisa reclamos.

Evaluacion de logica:

- Backend debe validar rol admin en cada endpoint.
- Resolver disputa a favor vendedor debe restaurar oferta.
- Resolver debe exigir nota.
- Ban debe invalidar sesion.
- Todas las acciones admin deben auditarse.

Evaluacion de diseno:

- Mostrar informacion suficiente antes de resolver: comprador, vendedor, vouchers, montos, timeline.
- Confirmacion antes de ban/resolver.

Errores/riesgos encontrados:

- Falta auditoria admin visible.

Criterio de aprobado:

- Admin puede resolver sin romper saldos y queda registro.

### 17. Cuentas bancarias

Pantallas a revisar:

- `BankAccountsScreen`
- `BankAccountsViewModel`
- APIs `GET /bank-accounts`, `POST /bank-accounts`, `DELETE /bank-accounts/{id}`, `PATCH /bank-accounts/{id}/set-default`

Flujo esperado:

1. Usuario lista cuentas.
2. Agrega cuenta.
3. Marca cuenta predeterminada.
4. Usa esa cuenta para publicar/comprar.
5. Elimina solo cuentas no bloqueadas por transacciones activas.

Evaluacion de logica:

- Validar formatos.
- Verificar que marcar cuenta principal actualiza la lista y se use en publicar/comprar.
- No permitir borrar cuenta usada en transaccion activa.

Evaluacion de diseno:

- Distinguir cuenta predeterminada.
- Acciones claras: agregar, eliminar, predeterminar.

Criterio de aprobado:

- Metodo de pago predeterminado queda claro y se usa en flujos.

### 18. Perfil y editar perfil

Pantallas a revisar:

- `ProfileScreen`
- `EditProfileScreen`
- `ProfileViewModel`
- `EditProfileViewModel`
- APIs `GET /users/me`, `PATCH /users/profile`

Flujo esperado:

1. Usuario entra a perfil.
2. Ve nombre, rol, KYC, reputacion y accesos.
3. Edita datos permitidos.
4. Puede cerrar sesion.
5. Si es admin, ve acceso admin.

Evaluacion de logica:

- Rol y KYC deben venir del backend actual.
- Logout debe borrar tokens y datos sensibles.
- Editar perfil no debe permitir cambiar rol/KYC.

Evaluacion de diseno:

- Estado KYC visible.
- Accesos agrupados: cuenta, seguridad, soporte, legal.

Errores/riesgos encontrados:

- Riesgo de confiar en rol cacheado localmente.

Criterio de aprobado:

- Perfil refleja datos reales y logout deja app limpia.

### 19. Notificaciones

Pantallas a revisar:

- `NotificationsScreen`
- `NotificationsViewModel`
- Servicio FCM Android
- API `/notifications`

Flujo esperado:

1. Backend crea notificacion interna.
2. Backend envia push si hay token activo.
3. Usuario ve badge/contador.
4. Usuario abre notificaciones.
5. Puede marcar leida o eliminar.

Evaluacion de logica:

- Permiso Android 13+ concedido.
- FCM token registrado.
- Multi-dispositivo si aplica.
- Badge no debe depender de polling caro.

Evaluacion de diseno:

- Diferenciar no leidas.
- Estado vacio.
- Acciones claras.

Errores/riesgos encontrados:

- Algunos usuarios no tenian token registrado.
- Token unico por usuario es fragil.
- Polling cada 4 segundos en bottom bar puede consumir recursos.

Criterio de aprobado:

- Push llega a telefono real y aparece tambien en inbox.

### 20. Soporte, reclamos, ayuda y legales

Pantallas a revisar:

- `ComplaintsScreen`
- `HelpScreen`
- `TermsScreen`
- `PrivacyScreen`
- `AboutScreen`
- APIs `POST /complaints`, `GET /complaints/my-complaints`

Flujo esperado:

1. Usuario consulta ayuda/legal.
2. Usuario registra reclamo.
3. Backend guarda reclamo.
4. Usuario ve sus reclamos.
5. Admin resuelve reclamo.

Evaluacion de logica:

- Reclamo debe exigir categoria/mensaje.
- Admin debe responder o marcar resuelto.
- Terminos internos y TermsFeed deben estar alineados.

Evaluacion de diseno:

- Ayuda no debe prometer funciones inexistentes.
- Terminos y privacidad legibles.
- Reclamo debe dar confirmacion.

Errores/riesgos encontrados:

- Textos pueden prometer OCR/cifrado/auditorias que deben existir realmente.

Criterio de aprobado:

- Soporte y legal reflejan lo que el sistema realmente hace.

### 21. Chatbot

Pantallas a revisar:

- `ChatBotScreen`
- `ChatBotViewModel`
- `ChatHistoryStore`

Flujo esperado:

1. Usuario abre asistente desde Market.
2. Pregunta sobre uso de la app.
3. Chatbot responde dentro de alcance.
4. Historial queda local.

Evaluacion de logica:

- No debe inventar reglas financieras.
- No debe prometer acciones que el sistema no tiene.
- Debe manejar error de API externa.

Evaluacion de diseno:

- FAB no debe tapar acciones.
- Animacion debe ser moderada.
- Chat debe ser legible en pantalla pequena.

Errores/riesgos encontrados:

- Prompt estatico puede quedar desalineado con reglas reales.
- FAB grande puede tapar contenido.

Criterio de aprobado:

- Chatbot ayuda sin contradecir la logica real del sistema.

### 22. Prueba final de flujo completo

Pantallas a revisar:

- Todas las anteriores, en recorrido real.

Flujo esperado:

1. Usuario vendedor registra cuenta.
2. Acepta terminos.
3. Sube KYC.
4. Admin aprueba KYC.
5. Vendedor agrega cuenta bancaria.
6. Vendedor publica oferta.
7. Comprador registra cuenta.
8. Comprador aprueba KYC.
9. Comprador entra a Market.
10. Comprador filtra monedas.
11. Comprador inicia compra.
12. Vendedor acepta.
13. Comprador sube voucher.
14. Vendedor sube voucher.
15. Vendedor confirma.
16. Comprador ve recibo.
17. Comprador califica.
18. Ambos ven historial actualizado.
19. Repetir una operacion con disputa.
20. Admin resuelve disputa.

Evaluacion de logica:

- No tocar base de datos manualmente durante la prueba.
- Cada paso debe producir el estado esperado.
- Cada notificacion debe aparecer.
- Cada monto debe coincidir.

Evaluacion de diseno:

- Ninguna pantalla se corta.
- Ningun texto se superpone.
- Botones importantes siempre visibles o accesibles con scroll.

Errores/riesgos encontrados:

- El flujo completo depende de probar en orden `accepted`, KYC admin, terminos persistidos y restauracion de ofertas en disputas.

Criterio de aprobado:

- Se puede grabar una demo completa sin errores backend, sin hacks y sin editar datos manualmente.

## Mapa general del sistema

### Pantallas Android detectadas

- Inicio: `welcome`
- Autenticacion: `login`, `register`, `forgot_pass`
- KYC: `kyc`, `kyc_summary`
- Operacion principal: `market`, `publish`, `pending`, `history`, `transaction/{transactionId}`, `tx_detail/{transactionId}`, `receipt/{transactionId}`
- Perfil y cuenta: `profile`, `edit_profile`, `bank_accounts`
- Reputacion: `rating/{transactionId}/{score}`, `reviews`
- Soporte/legal: `complaints`, `terms`, `privacy`, `about`, `help`, `chatbot`
- Disputas: `my_disputes`, `register_dispute/{transactionId}`, `dispute_detail/{disputeId}`
- Notificaciones: `notifications`
- Administracion: `admin`

### APIs backend detectadas

- Auth: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me`, `POST /auth/logout`
- Usuarios: `GET /users/me`, `PATCH /users/profile`, `POST /users/kyc`, `POST /users/signature`, `GET /users/{user_id}`
- Ofertas: `GET /offers`, `GET /offers/{id}`, `POST /offers`, `GET /offers/my-offers`, `POST /offers/match`, `PATCH /offers/{id}`, `DELETE /offers/{id}`
- Transacciones: `GET /transactions`, `GET /transactions/pending`, `GET /transactions/vouchers`, `GET /transactions/{id}`, `POST /transactions`, `POST /transactions/{id}/voucher`, `POST /transactions/{id}/vendor-voucher`, `POST /transactions/{id}/confirm`, `POST /transactions/{id}/dispute`, `PATCH /transactions/{id}/status`, `GET /transactions/disputes`
- Cuentas bancarias: `GET /bank-accounts`, `POST /bank-accounts`, `DELETE /bank-accounts/{id}`, `PATCH /bank-accounts/{id}/set-default`
- Tasas: `GET /exchange/rates`, `GET /exchange/convert`, `GET /exchange/currencies`
- Notificaciones: `POST /notifications/fcm-token`, `GET /notifications`, `GET /notifications/unread-count`, `GET /notifications/{id}`, `PATCH /notifications/{id}/read`, `POST /notifications/mark-all-read`, `DELETE /notifications/{id}`, `DELETE /notifications`
- Ratings: `GET /ratings/received`, `POST /ratings`
- Disputas: `GET /disputes/my-disputes`, `GET /disputes/{id}`
- Reclamos: `POST /complaints`, `GET /complaints/my-complaints`
- Admin: dashboard, usuarios, ban, disputas, tomar/resolver disputa, reclamos/resolver
- Uploads: subida y servido local de imagenes

## Hallazgos importantes

### H2. Terminos aceptados solo existen en UI

Flujo afectado: registro.

La pantalla de registro obliga a marcar terminos antes de crear cuenta y abre el enlace externo de TermsFeed. El backend persiste `terms_accepted_at`, version y URL.

Impacto: no hay evidencia auditable de consentimiento.

Recomendacion: agregar campos en registro:

- `terms_accepted = true`
- `terms_version`
- `terms_url`
- `terms_accepted_at`

El backend rechaza registro si no llega aceptacion valida.

### H2. Notificaciones FCM dependen de un unico token

Flujo afectado: login, registro, notificaciones push.

La app registra FCM al iniciar sesion y ahora tambien al registrarse. El backend guarda token por usuario, pero no se ve soporte multi-dispositivo, revocacion por logout ni limpieza de tokens invalidos.

Impacto: si el usuario usa mas de un telefono, reinstala la app, niega permiso `POST_NOTIFICATIONS` o no vuelve a iniciar sesion, las notificaciones pueden no llegar.

Recomendacion: guardar tokens por dispositivo, con `device_id`, plataforma, fecha de ultimo uso y estado. Enviar push a todos los tokens activos y borrar los que Firebase marque invalidos.

### H2. El backend confirma transacciones sin verificacion real del comprobante

Flujo afectado: voucher y liberacion.

La app habla de comprobantes y algunas pantallas sugieren validacion automatica/OCR, pero el backend solo exige que existan voucher del comprador y voucher del vendedor antes de confirmar. No hay OCR, validacion bancaria ni conciliacion de monto.

Impacto: falsa percepcion de seguridad.

Recomendacion: ajustar textos para no prometer OCR si no existe, o implementar validacion real con estado `voucher_under_review`.

## Hallazgos medios y de deuda tecnica

### H3. Polling cada 4 segundos en bottom bar

El bottom bar consulta transacciones del comprador y pendientes del vendedor cada 4 segundos.

Impacto: consumo de bateria/red y carga innecesaria.

Recomendacion: subir intervalo, usar unread/pending-count dedicado o combinar con push/WebSocket cuando exista.

### H3. Tokens en DataStore sin cifrado

`TokenManager` usa preferencias/DataStore. Para una app financiera, access/refresh tokens deberian guardarse con Android Keystore/EncryptedDataStore o equivalente.

### H3. Textos con codificacion rota en backend

Se ven cadenas como `TransacciÃ³n`, `invÃ¡lida`, `mÃ­nimo`. Esto puede salir al usuario si el backend retorna errores.

Recomendacion: normalizar archivos a UTF-8 y revisar respuestas de error.

### H3. Falta centralizar errores

Algunos repositorios parsean mensajes del backend y otros usan mensajes genericos. Esto genera UX irregular.

Recomendacion: crear un mapper unico de errores API -> mensaje usuario -> accion sugerida.

### H3. Chatbot depende de contexto local y prompts estaticos

El chatbot puede ayudar, pero si responde sobre reglas financieras, estados o soporte, debe alinearse con la logica real del backend y no prometer funciones inexistentes.

Recomendacion: alimentar el chatbot con FAQ versionada o limitarlo a orientacion general.

## Revision flujo por flujo

### 1. Inicio / Welcome

Flujo actual: la app abre en `AuthGate`; si hay sesion va a Mercado y si no hay sesion muestra Welcome con dos caminos: iniciar sesion o registrarse.

Logica: correcta para usuarios nuevos y sesiones guardadas.

Diseno: buena separacion inicial. Conviene que el logo, botones y fondo compartan el mismo lenguaje visual del login.

Acciones:

- Mantener revision de token al abrir app.
- Mostrar loader corto mientras se decide destino.
- Mantener solo dos acciones primarias: login y registro.

### 2. Login

Flujo actual: usuario ingresa email/password, recibe tokens, se guarda sesion y se registra FCM.

Logica: base correcta. Riesgo si FCM falla silenciosamente o permiso Android esta denegado.

Diseno: la pantalla azul con icono superior funciona bien como identidad. El password tiene icono de visibilidad, correcto.

Acciones:

- Mostrar estado de sesion con mensajes claros.
- Registrar FCM tambien si cambia token.
- No bloquear login si FCM falla, pero registrar log y reintentar.

### 3. Registro + terminos + KYC

Flujo actual: formulario por pasos, documentos KYC, password y aceptacion obligatoria de terminos.

Logica: Android obliga checkbox, backend persiste aceptacion y KYC queda en revision hasta aprobacion admin.

Diseno: el header compacto va mejor, pero stepper/header estan muy sensibles a offsets. Numeros del stepper son clicables para retroceder, eso esta bien si solo permite volver a pasos ya completados.

Acciones:

- Mantener aceptacion de terminos persistida en backend.
- Convertir KYC a flujo `submitted/approved/rejected`.
- Evitar offsets manuales; usar layout con alturas proporcionales y constraints.
- Probar en 360x640, 390x844 y pantalla grande.

### 4. Recuperar password

Flujo actual: pantalla existe, pero no se confirmo endpoint backend de recuperacion.

Riesgo: si solo es UI, el usuario queda sin recuperacion real.

Acciones:

- Implementar endpoint de solicitud y reset con token.
- Agregar expiracion y limites por email/IP.

### 5. Mercado

Flujo actual: lista ofertas, filtra moneda origen/destino, muestra tasas arriba y permite iniciar compra/matching.

Logica: funcional. Matching depende del backend y las tasas del carrusel se consumen respecto a PEN.

Diseno: la UI tiene identidad fuerte, pero el carrusel debe ser realmente continuo, sin espacios blancos ni reinicios visibles. Los filtros deben mostrar pocas opciones visibles y scroll para el resto, sin buscador.

Acciones:

- Crear endpoint ticker backend con todos los pares contra PEN.
- Mantener carrusel con lista duplicada/virtual y ancho suficiente.
- Evitar que chips de tasas tengan ancho variable extremo.
- Agregar estado sin conexion mas especifico.

### 6. Publicar oferta

Flujo actual: vendedor publica moneda, monto, precio, min/max y metodos de pago.

Logica: backend permite crear si monto/precio son positivos. Falta validar rol, KYC, usuario activo y consistencia min/max.

Diseno: debe priorizar claridad de monto que vendo, moneda que recibo y tasa. Para fintech, menos decoracion y mas validacion inmediata.

Acciones:

- Validar `min_transaction <= max_transaction <= amount`.
- Bloquear publicacion si no hay KYC aprobado.
- Usar cuenta bancaria predeterminada.

### 7. Mis ofertas

Flujo actual: lista ofertas propias y permite gestion.

Logica: backend permite editar precio, status y limites. `available_amount` no debe editarse directamente.

Acciones:

- Restringir cambios de disponibilidad si hay transacciones activas.
- Guardar historial de cambios de tasa/monto.

### 8. Crear transaccion / compra

Flujo actual: comprador toma una oferta, backend bloquea oferta con `get_by_id_for_update`, descuenta disponibilidad y crea transaccion `pending`.

Logica: buena base: evita comprar oferta propia, valida min/max/full y disponibilidad.

Riesgos:

- No se valida KYC del comprador.
- No se recalcula `amount_to` en backend con tasa de la oferta; acepta lo que manda cliente.

Acciones:

- Calcular `amount_to = amount_from * offer.price_per_unit` en backend.
- Validar comprador activo/KYC si aplica.
- Usar idempotency key para evitar doble compra por doble tap.

### 9. Aceptacion del vendedor

Flujo actual esperado: vendedor acepta orden y comprador sube comprobante.

Logica: backend soporta estado `accepted` desde `pending`, restringido al vendedor.

Acciones:

- Implementar endpoint/transicion `accept`.
- Solo vendedor puede aceptar.
- Notificar al comprador cuando pasa a `accepted`.
- El timer debe arrancar desde `accepted_at`, no solo desde `created_at`.

### 10. Voucher del comprador

Flujo actual: Android sube `image_base64` a `POST /transactions/{id}/voucher`; backend sube imagen, crea voucher y cambia estado a `voucher_uploaded`.

Logica: contrato Android/backend esta alineado para base64.

Riesgos:

- No valida tamano, tipo real de archivo ni compresion maxima en backend.
- No valida monto/banco/datos del comprobante.

Acciones:

- Limitar peso y tipos.
- Agregar estado de revision si se implementa validacion.

### 11. Voucher del vendedor y confirmacion

Flujo actual: vendedor sube comprobante propio por `vendor-voucher`; luego confirma si existe voucher comprador y vendedor.

Logica: la confirmacion exige ambos comprobantes, lo cual reduce errores. Pero no hay revision objetiva.

Diseno: la pantalla debe dejar muy claro que "confirmar y liberar" es irreversible.

Acciones:

- Confirmacion modal con resumen de monto, moneda y contraparte.
- Registrar `confirmed_at`.
- Evitar confirmar si hay disputa abierta.

### 12. Recibo e historial

Flujo actual: al completar, backend genera PDF y envia correo al comprador. Android tiene pantalla de recibo/historial.

Logica: correcta en base. Falta verificar reintentos si falla email o upload de PDF.

Acciones:

- No depender del email para que el usuario pueda descargar recibo.
- Registrar eventos de envio/reintento.

### 13. Cancelacion / pausa / cierre

Flujo actual: `PATCH /transactions/{id}/status` permite `cancelled`, `paused`, `closed`.

Logica: cancelacion restaura oferta solo si status anterior esta en `pending` o `voucher_uploaded`.

Riesgos:

- `accepted` tambien debe restaurar oferta si se cancela.
- `paused` no tiene reglas claras de reanudacion.

Acciones:

- Definir maquina de estados completa.
- Documentar quien puede ejecutar cada transicion.

### 14. Disputas usuario

Flujo actual: participante abre disputa si la transaccion no esta completada ni cancelada. Backend evita duplicados y cambia status a `disputed`.

Logica: buena base.

Riesgo: no hay adjuntos/evidencias extra en disputa, solo motivo/descripcion.

Acciones:

- Permitir adjuntos.
- Congelar acciones sensibles mientras status es `disputed`.

### 15. Admin disputas

Flujo actual: admin lista, toma y resuelve disputas.

Logica: resolver a favor comprador completa transaccion e incrementa totales. Resolver a favor vendedor cancela y restaura oferta.

Diseno: debe mostrar evidencias, comprobantes, usuario comprador/vendedor y timeline antes de resolver.

Acciones:

- Corregir restauracion de oferta.
- Agregar notas obligatorias de resolucion.
- Auditar todas las acciones admin.

### 16. Calificaciones y reviews

Flujo actual: usuario califica despues de transaccion y puede ver reviews recibidas.

Riesgos:

- Confirmar si backend impide duplicar rating por transaccion.
- Confirmar si solo participantes de transaccion completada pueden calificar.

Acciones:

- Backend debe validar transaccion completada y participante.
- Mostrar rating promedio y conteo con datos reales.

### 17. Cuentas bancarias

Flujo actual: crear/listar/eliminar cuentas; backend tiene default.

Riesgos:

- No usar default en app complica seleccion.
- Debe validarse banco, CCI/cuenta, moneda y titular.

Acciones:

- Mantener set-default disponible en Android.
- Validar formatos por banco.

### 18. Notificaciones

Flujo actual: backend crea notificaciones internas y envia push si hay FCM token. Android lista, marca leidas y muestra contador.

Riesgos:

- Token unico por usuario.
- Permiso Android 13+ puede estar denegado.
- No hay diagnostico visible al usuario.

Acciones:

- Pantalla de ajustes que indique permiso de notificaciones.
- Multi-token por dispositivo.
- Reintentos y limpieza de tokens invalidos.

### 19. Perfil / editar perfil

Flujo actual: muestra datos, rol, KYC, accesos a legal/soporte/admin si corresponde.

Riesgos:

- Rol visual "Experto/Basico" debe coincidir con permisos reales backend.
- Editar perfil debe limitar campos sensibles.

Acciones:

- No confiar en rol guardado localmente para permisos.
- Refrescar `/auth/me` o `/users/me` al entrar.

### 20. Reclamos, ayuda, legales

Flujo actual: existen pantallas de ayuda, terminos, privacidad y reclamos.

Riesgos:

- Algunos textos prometen cifrado, OCR o controles que deben existir realmente.
- Terminos externos y terminos internos pueden divergir.

Acciones:

- Alinear textos legales con capacidades reales.
- Usar una fuente unica para terminos vigentes.

### 21. Chatbot

Flujo actual: asistente en mercado, con historial local.

Riesgos:

- Puede responder sobre temas sensibles sin estar amarrado a la verdad del sistema.
- FAB grande y animado puede tapar contenido en pantallas pequenas.

Acciones:

- Reducir movimiento si usuario tiene reduce motion.
- Limitar respuestas a FAQs verificadas.

### 22. Admin usuarios/reclamos

Flujo actual: admin dashboard, usuarios, ban, disputas, reclamos.

Riesgos:

- Acciones criticas requieren auditoria.
- Ban debe invalidar tokens/sesiones activas.

Acciones:

- Log admin: quien, que, cuando, motivo, antes/despues.
- Invalidar tokens de usuario baneado.

## Revision de diseno

### Fortalezas

- Identidad visual clara con azul principal, logo y estilo fintech.
- Login y registro tienen una direccion visual consistente.
- Bottom bar organiza bien las tareas principales: mercado, pendientes, publicar y perfil.
- El flujo de registro por pasos reduce carga cognitiva.
- Los selectores con altura limitada son mejores que menus largos a pantalla completa.

### Riesgos visuales

- Header azul, logo y stepper dependen mucho de offsets manuales; eso puede romperse por densidad, notch o tamanos de pantalla.
- El stepper puede quedar demasiado cerca del header o superpuesto si cambia el contenido.
- El carrusel de tasas debe evitar espacios blancos, reinicios visibles y chips cortados.
- Algunas pantallas usan textos pequenos; revisar legibilidad en telefono real.
- El FAB de chatbot es grande y pulsante; puede distraer y tapar acciones.
- Hay riesgo de mezclar cards dentro de cards en pantallas operativas, lo cual reduce claridad.

### Recomendaciones de UI

- Crear componentes reutilizables: `AuthHeader`, `StepIndicator`, `CurrencySelector`, `RateTicker`, `StatusBadge`, `TransactionTimeline`.
- Definir tamanos fijos/responsivos para header y stepper en lugar de offsets.
- Probar cada pantalla en pequeno, mediano y grande.
- Reducir animacion cuando no aporta al flujo.
- Usar estados vacios con una accion clara.
- Mantener formularios densos pero respirables: labels claros, errores debajo del campo, botones siempre visibles o al final del scroll.
- Para menus de monedas: mostrar 5 visibles, scroll para el resto, sin buscador como se pidio.

## Prioridad de correccion

### Antes de demo o entrega

1. Probar estado `accepted` en flujo real comprador/vendedor.
2. Probar KYC en revision y aprobacion admin.
3. Probar bloqueo de publicar/comprar sin KYC aprobado.
4. Probar registro con y sin aceptacion de terminos.
5. Probar restauracion de oferta en disputa a favor del vendedor.
6. Probar calculo de `amount_to` en backend al crear transaccion.

### Siguiente sprint

1. Endpoint ticker contra PEN con cache.
2. Auth gate al inicio para sesiones existentes.
3. Multi-token FCM por dispositivo.
4. Set-default de cuentas bancarias en Android.
5. Centralizar errores y normalizar codificacion UTF-8.
6. Auditoria de acciones admin.

### Mejoras posteriores

1. OCR/validacion de comprobantes o ajustar textos para no prometerlo.
2. Reemplazar polling de bottom bar por endpoint contador o push.
3. Encriptar tokens locales.
4. Adjuntos en disputas.
5. Pruebas visuales de Compose con capturas.

## Plan de pruebas recomendado

### Pruebas backend

- Auth: registro sin terminos debe fallar; registro con terminos debe guardar version/timestamp.
- KYC: subir documentos debe dejar `submitted`, no `verified`.
- Ofertas: comprador/no verificado no puede publicar.
- Transaccion: no permite comprar oferta propia; respeta min/max/full; descuenta disponibilidad una sola vez.
- Aceptacion: solo vendedor puede pasar `pending -> accepted`.
- Voucher: comprador sube voucher y pasa a `voucher_uploaded`.
- Confirmacion: vendedor no puede confirmar sin voucher comprador y vendedor.
- Cancelacion: restaura oferta para `pending`, `accepted`, `voucher_uploaded`.
- Disputa: abre una sola disputa por transaccion y bloquea confirmacion.
- Resolucion admin: favor comprador completa; favor vendedor cancela y restaura disponibilidad.

### Pruebas Android

- Registro completo en pantalla pequena sin que header/stepper tapen campos.
- Terminos obligatorio y link abre navegador.
- Menus de monedas muestran 5 opciones visibles y scroll.
- Carrusel de tasas no deja huecos ni reinicios visibles.
- Login en telefono real pide permiso de notificaciones.
- Vendedor recibe pendiente, acepta, sube voucher y confirma.
- Comprador sube comprobante y recibe recibo.
- Disputa puede abrirse desde transaccion activa.

### Pruebas end-to-end

1. Usuario A registra, KYC queda en revision.
2. Admin aprueba KYC.
3. Usuario A publica oferta USD/PEN.
4. Usuario B compra parcial.
5. Usuario A acepta.
6. Usuario B sube voucher.
7. Usuario A sube voucher y confirma.
8. Usuario B ve recibo y califica.
9. Repetir con disputa y resolucion admin.

## Conclusiones

La app esta cerca de ser demostrable, pero la logica de transacciones necesita alinearse con urgencia. El backend debe ser la fuente de verdad para estados, permisos, KYC, montos y matching. La UI ya comunica una experiencia fintech atractiva, pero conviene convertir los ajustes visuales recientes en componentes estables para que no dependan del emulador exacto.

La prioridad no es agregar mas pantallas; es cerrar las reglas centrales para que cada boton que el usuario ve tenga una transicion valida, auditable y segura en el backend.
