# PeruExchange P2P — Documentación de Flujos + Evaluación UX/UI
> Revisión completa Android (Jetpack Compose) + Backend (Flask)  
> Fecha: 2026-06-08

---

## ÍNDICE

1. [Mapa de pantallas y rutas](#1-mapa-de-pantallas-y-rutas)
2. [Flujos completos (Android ↔ Backend)](#2-flujos-completos-android--backend)
3. [Evaluación UX/UI — Heurísticas de Nielsen](#3-evaluación-uxui--heurísticas-de-nielsen)
4. [Problemas encontrados por gravedad](#4-problemas-encontrados-por-gravedad)
5. [Estado de sprints](#5-estado-de-sprints)
6. [Usuarios de prueba](#6-usuarios-de-prueba)
7. [Arrancar el proyecto](#7-arrancar-el-proyecto)

---

## 1. Mapa de pantallas y rutas

| Pantalla | Ruta | Quién la ve | BottomBar |
|---|---|---|---|
| `LoginScreen` | `login` | Todos (sin sesión) | No |
| `RegisterScreen` | `register` | Todos (sin sesión) | No |
| `ForgotPasswordScreen` | `forgot_pass` | Todos (sin sesión) | No |
| `KycScreen` | `kyc` | Post-registro | No |
| `MarketScreen` | `market` | Buyer / Vendor / Admin | Sí |
| `PublishScreen` | `publish` | Vendor / Admin | Sí |
| `TransactionScreen` | `transaction/{id}` | Buyer (activo) | No |
| `ReceiptScreen` | `receipt/{id}` | Buyer (completado) | No |
| `RatingScreen` | `rating/{id}/{score}` | Buyer post-transacción | No |
| `VendorInboxScreen` | `vendor` | Vendor | No |
| `HistoryScreen` | `history` | Todos | No |
| `TransactionDetailScreen` | `tx_detail/{id}` | Todos | No |
| `ProfileScreen` | `profile` | Todos | Sí |
| `EditProfileScreen` | `edit_profile` | Todos | No |
| `BankAccountsScreen` | `bank_accounts` | Todos | No |
| `ReviewsScreen` | `reviews` | Todos | No |
| `NotificationsScreen` | `notifications` | Todos | No |
| `MyOffersScreen` | `my_offers` | Vendor / Admin | No |
| `MyDisputesScreen` | `my_disputes` | Todos | No |
| `RegisterDisputeScreen` | `register_dispute/{id}` | Buyer en disputa | No |
| `DisputeDetailScreen` | `dispute_detail/{id}` | Todos | No |
| `ComplaintsScreen` | `complaints` | Todos | No |
| `AdminScreen` | `admin` | Admin | No |
| `TermsScreen` | `terms` | Todos | No |
| `PrivacyScreen` | `privacy` | Todos | No |
| `AboutScreen` | `about` | Todos | No |
| `HelpScreen` | `help` | Todos | No |

---

## 2. Flujos completos (Android ↔ Backend)

---

### FLUJO 1 — Registro de usuario

**Pantallas:** `RegisterScreen` → `KycScreen` → `MarketScreen`

#### Android
```
RegisterScreen
  Campos: Nombre completo · Email · DNI (max 8 dígitos) · Contraseña · Confirmar contraseña
  Validaciones frontend:
    - Todos los campos obligatorios
    - Contraseña mínimo 8 caracteres
    - Confirmar contraseña debe coincidir (error inline en tiempo real)
    - DNI máximo 8 dígitos numéricos
    - Acepto Términos y Condiciones (checkbox requerido para habilitar botón)
  Estado Loading: CircularProgressIndicator en botón
  Estado Error: Banner rojo con icono Warning + mensaje del backend
  Al éxito: LaunchedEffect(isSuccess) → navega a KycScreen

KycScreen (paso obligatorio post-registro)
  Paso 1 — DNI Frontal: foto con cámara, FileProvider URI
  Paso 2 — DNI Posterior: foto con cámara
  Paso 3 — Selfie: foto con cámara sosteniendo DNI
  Permiso de cámara: solicita en runtime (ActivityResultContracts.RequestPermission)
  Al enviar: navega a MarketScreen con popUpTo(0) inclusive
```

#### Backend
```
POST /api/v1/auth/register
  Body: { email, password, full_name, dni?, phone?, role? }
  Validaciones:
    - email y password presentes → 400 MISSING_FIELDS
    - email duplicado → 409 ConflictError "Email already registered"
    - dni duplicado → 409 ConflictError "DNI already registered"
  Acción: crea User en BD, set_password(bcrypt), db.session.commit()
  Respuesta 201: { id, email, full_name, role, kyc_verified, rating,
                   avatar_url, access_token, refresh_token }

POST /api/v1/uploads/kyc  (KycViewModel.submitKyc)
  Body: multipart/form-data con dni_front, dni_back, selfie
  Marca kyc_verified = True en el usuario
  Respuesta 200: { message: "KYC submitted" }
```

**Flujo completo:**
```
User llena form → RegisterScreen.register(email, pw, fullName, dni)
  → RegisterViewModel.register() valida campos
    → AuthRepositoryImpl.register() → POST /auth/register
      ← 201 { tokens... }
    → TokenManager.saveSession(tokens, userId, role, name, email)
    → isSuccess = true
  → navController.navigate(Screen.Kyc)
    → User fotografía DNI frontal → dni_back → selfie
    → KycViewModel.submitKyc() → POST /uploads/kyc
    → isSuccess = true → onNavigateBack() → navController.navigate(Market) popUpTo(0)
```

---

### FLUJO 2 — Login

**Pantallas:** `LoginScreen` → `MarketScreen`

#### Android
```
LoginScreen
  Campos: Email · Contraseña (con toggle de visibilidad)
  Demo credentials card visible en pantalla (BCP colores)
  Estado Loading: CircularProgressIndicator
  Estado Error: Banner animado con mensaje
  Al éxito: navega a Market, popUpTo(Login) inclusive
```

#### Backend
```
POST /api/v1/auth/login
  Validaciones:
    - email y password presentes → 400
    - usuario no encontrado o password incorrecto → 401 AuthenticationError
    - is_active = False → 401 "Account is inactive"
  Acción: crea notificación "Inicio de sesión exitoso", db.commit()
  Respuesta 200: { id, email, full_name, role, kyc_verified, rating,
                   avatar_url, access_token, refresh_token }
```

---

### FLUJO 3 — Ver mercado y aceptar oferta (Buyer)

**Pantallas:** `MarketScreen` → `TransactionScreen`

#### Android
```
MarketScreen
  Auto-refresh tasas de cambio: cada 8 segundos
  Filtros: "Tengo" (USD/EUR/USDT) · "Quiero" (PEN/USD/EUR/USDT)
  Ticker: USD/PEN · EUR/PEN con valor y variación %
  Tarjetas de oferta: vendor, badge KYC, rating, "En línea", precio, monto, métodos de pago
  Estados: Loading · Error (con botón Actualizar) · Empty "Sin ofertas disponibles"
  
  Al tocar oferta → Dialog de Matching:
    - Selector: "Compra Total" o "Compra Parcial"
    - Input monto con validación min/max en tiempo real
    - Selector de cuenta bancaria del comprador (del vendedor se muestra)
    - Si no tiene cuentas → alerta con botón "Agregar cuenta"
    - Botón "Confirmar" → crea transacción
```

#### Backend
```
GET /api/v1/exchange/rates
  Respuesta: [{ from_currency, to_currency, rate, change_24h }]

GET /api/v1/offers
  Query params: ?from_currency=USD&to_currency=PEN
  Excluye: ofertas propias (by vendor_id ≠ current_user_id)
  Excluye: ofertas con status ≠ "active"
  Respuesta: lista de ofertas con vendor info

POST /api/v1/offers/match   (matching automático — mejor precio)
  Body: { from_currency, to_currency, amount }
  Respuesta: mejor oferta disponible

POST /api/v1/transactions
  Body: { offer_id, amount_from, buyer_bank_account_id }
  Validaciones:
    - Oferta activa y existente → 404
    - No comprar propia oferta → 400 "No puedes comprar tu propia oferta"
    - Monto ≥ min_transaction y ≤ max_transaction → 400
    - Monto ≤ available_amount → 400
  Acción: crea Transaction con status="pending", deduce amount de la oferta
  Notifica al vendor: "Nueva orden de compra"
  Respuesta 201: { id, status, amount_from, amount_to, rate, buyer_info, vendor_info }
```

---

### FLUJO 4 — Transacción completa P2P (Buyer + Vendor)

**Pantallas:** `TransactionScreen` → `ReceiptScreen` → `RatingScreen`

#### Android (Buyer)
```
TransactionScreen (auto-refresh cada 5 segundos)
  Header: countdown timer 15 minutos (solo visible al inicio)
  Timeline: Pagar → Voucher → Confirmar → Liberado

  Estado "pending":
    - Info bancaria del vendedor (cuenta destino)
    - Monto a transferir
    - Zona de upload de voucher (imagen)
    - Botón "Cancelar operación"

  Estado "accepted":
    - Banner "¡El vendedor aceptó tu orden!"
    - Misma zona de upload de voucher
    - Botón "Subir Comprobante de Pago"

  Estado "voucher_uploaded":
    - Banner "Esperando confirmación del vendedor..."
    - Botón "Abrir Disputa" (si no confirma)

  Estado "completed":
    - Vista de éxito con checkmark
    - Botón "Ver Comprobante" → ReceiptScreen
    - Dialog de calificación inline (5 estrellas)

  Estado "disputed":
    - Banner naranja de disputa activa

  Estado "cancelled":
    - Banner gris, transacción cancelada
```

#### Android (Vendor) — VendorInboxScreen
```
VendorInboxScreen (auto-refresh cada 5 segundos)
  Tarjetas por transacción pendiente:
    - Status "pending" → Botón "Aceptar orden de compra" (azul)
    - Status "accepted" → Banner "Esperando pago del comprador"
    - Status "voucher_uploaded" → Botón "Confirmar Pago y Liberar" (verde)
    - Botón "Cancelar operación" (rojo) siempre disponible
  Estado vacío: "Sin órdenes activas"
  Botón Refresh manual en TopAppBar
```

#### Backend
```
PATCH /api/v1/transactions/{id}/status
  Body: { status: "accepted" | "cancelled" }
  Solo vendor puede aceptar/cancelar
  Notifica al buyer del cambio de estado

POST /api/v1/transactions/{id}/voucher
  Body: { image_base64 } (imagen en base64)
  Cambia status → "voucher_uploaded"
  Notifica al vendor: "Comprobante subido"

POST /api/v1/transactions/{id}/confirm
  Solo el vendor puede confirmar
  Status → "completed"
  Notifica al buyer: "¡Pago confirmado! Fondos liberados"
  Actualiza total_transactions del vendor

GET /api/v1/transactions/{id}
  Respuesta: transacción completa con buyer_name, vendor_name resueltos

GET /api/v1/transactions/pending
  Solo vendor: transacciones en status pending/accepted/voucher_uploaded
```

---

### FLUJO 5 — Comprobante y calificación

**Pantallas:** `ReceiptScreen` → `RatingScreen` → `MarketScreen`

#### Android
```
ReceiptScreen
  Comprobante P2P:
    - ID de transacción
    - Nombre comprador / vendedor (reales del backend)
    - Tasa aplicada
    - Monto acreditado (grande, color éxito)
  Botones:
    - "Descargar Comprobante" (funcionalidad pendiente)
    - "Calificar Vendedor" → RatingScreen
    - "Volver al Mercado" → MarketScreen

RatingScreen
  5 estrellas interactivas (tap para seleccionar)
  Texto dinámico: 1="Muy mala" · 3="Regular" · 5="¡Excelente!"
  Campo de comentario opcional
  Botón "Enviar Calificación" (habilitado solo si score > 0)
  Botón "Omitir por ahora" → Market (sin calificar)
```

#### Backend
```
POST /api/v1/ratings
  Body: { transaction_id, rated_user_id, score (1-5), comment? }
  Validaciones:
    - Transacción completada → 400
    - Solo el buyer puede calificar → 400
    - No calificar 2 veces la misma transacción → 409
  Acción: actualiza rating promedio del vendor (AVG)
  Respuesta 201: { id, score, comment, created_at }

GET /api/v1/ratings/received
  Respuesta: { average, total, distribution: {1:n, 2:n, ...5:n}, reviews: [...] }
```

---

### FLUJO 6 — Publicar oferta (Vendor)

**Pantallas:** `MyOffersScreen` → `PublishScreen` → `MyOffersScreen`

#### Android
```
PublishScreen
  Sección "Par de Divisas":
    - Dropdown "Ofrezco" (USD/EUR/USDT)
    - Dropdown "Recibo en" (PEN/USD/EUR)
    - Icono swap para intercambiar
    - Tasa de mercado en tiempo real (cargada del backend)

  Sección "Monto":
    - Campo decimal "Monto Total Disponible"

  Sección "Modo de Venta":
    - Card "Venta Completa" — comprador debe comprar todo
    - Card "Venta por Partes" — define rango min/max por transacción

  Sección "Tasa de Cambio":
    - "Tasa Mercado" (sin cambio)
    - "Venta Rápida" (-0.5%, más competitivo)
    - "Personalizada" → input manual

  Sección "Cuenta Bancaria":
    - Chip selector de cuentas registradas para esa moneda
    - Advertencia si no hay cuentas → botón "Agregar"

  Preview: Par · Tasa aplicada · Recibirás aprox · vs Mercado %
  
  Validaciones:
    - Monto > 0
    - Tasa > 0
    - Cuenta bancaria seleccionada
    - Para parcial: min ≤ max ≤ monto_total
```

#### Backend
```
GET /api/v1/exchange/rates  →  tasa en tiempo real

GET /api/v1/bank-accounts   →  cuentas del vendor por moneda

POST /api/v1/offers
  Body: {
    from_currency, to_currency,
    amount, rate,
    offer_type: "full" | "partial",
    min_transaction?, max_transaction?,
    bank_account_id
  }
  Validaciones:
    - Solo vendor/admin pueden publicar → 403
    - Cuenta bancaria pertenece al vendor → 400
    - min ≤ max ≤ amount → 400
  Respuesta 201: { id, status: "active", ... }
```

---

### FLUJO 7 — Gestión de cuentas bancarias

**Pantalla:** `BankAccountsScreen`

#### Android
```
BankAccountsScreen
  Bancos disponibles (chips scrollables):
    BCP · Interbank · BBVA · Yape · Plin · Wise · Binance · Otro
  
  Validación de número por banco:
    - Yape / Plin → 9 dígitos (celular)
    - BCP / Interbank / BBVA → 20 dígitos (CCI)
    - Internacional (Wise/Binance) → hasta 60 caracteres
  Indicador de validez: "✓ Número válido" (verde) o error (rojo)
  
  Selector de moneda (chips): PEN · USD · EUR · USDT
  
  Lista de cuentas registradas:
    - Inicial del banco en círculo de color
    - Nombre del banco + badge moneda
    - Número de cuenta
    - Nombre del titular
    - Botón eliminar (rojo)
  
  Estado vacío: "No tienes cuentas bancarias registradas."
```

#### Backend
```
GET /api/v1/bank-accounts
  Solo del usuario autenticado (JWT)
  Respuesta: [{ id, bank_name, account_number, currency, holder_name, is_default }]

POST /api/v1/bank-accounts
  Body: { bank_name, account_number, currency, holder_name }
  Validaciones (schemas.py):
    - Campos requeridos presentes
    - Formato de número según banco
    - No duplicar (bank_name + account_number único por usuario)
  Respuesta 201: cuenta creada

DELETE /api/v1/bank-accounts/{id}
  Valida: cuenta sin transacciones activas (pending/voucher_uploaded/disputed)
  Respuesta 200: { message: "Account deleted" }

PATCH /api/v1/bank-accounts/{id}/set-default
  Marca is_default = True, desmarca las demás del usuario
  Respuesta 200
```

---

### FLUJO 8 — Disputas

**Pantallas:** `TransactionScreen` → `RegisterDisputeScreen` → `MyDisputesScreen` → `DisputeDetailScreen`

#### Android
```
RegisterDisputeScreen
  Info banner: "Disputas revisadas en 5 días hábiles" (rojo)
  Razones (dropdown):
    - "El vendedor no liberó los fondos"
    - "El comprador no realizó el pago"
    - "El voucher no corresponde al monto"
    - "Fondos enviados al banco incorrecto"
    - "Otro motivo"
  Campo descripción (textarea, 5 líneas)
  Zona de evidencia (simulada, no funcional actualmente)
  Botón "Enviar Disputa" (rojo)

MyDisputesScreen
  Filtros: Todas · Abiertas · Resueltas
  Tarjeta de disputa:
    - ID de transacción
    - Status badge (rojo abierto, verde resuelto)
    - Razón
    - Fecha
    - Botón "Ver detalle"

DisputeDetailScreen
  Info: ID disputa, status, transacción asociada
  Montos: amount_from, amount_to, tasa
  Card de resolución (solo si status == "resolved"):
    - A favor de: Comprador / Vendedor
    - Nota del admin
    - Fecha resolución
```

#### Backend
```
POST /api/v1/transactions/{id}/dispute
  Validaciones:
    - Transacción existe y pertenece al usuario → 403
    - Status no puede ser "completed" → 400
  Acción: crea Dispute, cambia transaction.status = "disputed"
  Notifica: al vendor y al buyer
  Respuesta 201: { dispute_id, transaction_id, reason, status: "open" }

GET /api/v1/disputes/my-disputes
  Paginado: ?page=1&per_page=20
  Filtra por usuario autenticado (como buyer o vendor)
  Respuesta: { disputes: [...], total, pages }

GET /api/v1/disputes/{id}
  Detalle completo: razón, descripción, transacción asociada, resolución
```

---

### FLUJO 9 — Panel de administrador

**Pantalla:** `AdminScreen`

#### Android
```
AdminScreen (solo visible con role == "admin")
  Header card:
    - Volumen total en S/
    - Disputas pendientes (rojo si hay)
    - Total usuarios

  Tabs: "Disputas" | "Reclamos"

  Tab Disputas:
    - Status pills: En arbitraje · En revisión · Resueltas
    - DisputeCard por disputa:
      * ID, status badge, razón, descripción, fecha
      * Botón "✓ Comprador" (verde) / "✓ Vendedor" (naranja)
      * Tap en card → DisputeDetailScreen

  Tab Reclamos:
    - ComplaintCard:
      * "#RCL-{id corto}", tipo, status, descripción
      * Nota del admin (si existe)
      * Botón "Resolver Reclamo" → Dialog con campo nota
```

#### Backend
```
GET /api/v1/admin/dashboard
  Requiere: role == "admin"
  Respuesta: { total_users, total_transactions, total_volume, pending_disputes }

GET /api/v1/admin/disputes
  Requiere: role == "admin"
  Filtra: status "open" y "under_review" por defecto
  Respuesta: lista de disputas con info de comprador/vendedor

PATCH /api/v1/admin/disputes/{id}/resolve
  Body: { resolution: "favour_buyer" | "favour_vendor", resolution_note }
  Acción:
    - Cambia dispute.status = "resolved"
    - Registra resolved_by (admin_id), resolution_note, resolved_at
    - Notifica al buyer y vendor con el resultado
  Respuesta 200

GET /api/v1/admin/complaints
  Lista todos los reclamos con datos del usuario

PATCH /api/v1/admin/complaints/{id}/resolve
  Body: { admin_note }
  Requiere: admin_note no vacío
  Cambia complaint.status = "resolved"
  Respuesta 200
```

---

### FLUJO 10 — Reclamos (usuarios)

**Pantalla:** `ComplaintsScreen`

#### Android
```
Formulario "Nuevo Reclamo":
  Dropdown tipo de reclamo
  Textarea descripción
  Botón "Enviar Reclamo" (rojo)

Lista "Mis Reclamos":
  #RCL-{id}, tipo, status badge, fecha creación
```

#### Backend
```
POST /api/v1/complaints
  Body: { type, description }
  Tipos válidos: definidos en Complaint.VALID_TYPES
  Respuesta 201: { id, type, status: "pending", created_at }

GET /api/v1/complaints/my-complaints
  Solo del usuario autenticado
  Respuesta: lista de reclamos del usuario
```

---

### FLUJO 11 — Perfil y configuración

**Pantallas:** `ProfileScreen` → `EditProfileScreen` / `ReviewsScreen` / `HistoryScreen` / etc.

#### Android
```
ProfileScreen
  Hero card: avatar con iniciales, nombre, email, rol, rating
  Estadísticas: transacciones, % completadas, tiempo respuesta
  Badge KYC verificado
  Botones rápidos: "Pendientes" (→ VendorInboxScreen), "Mis Disputas"
  
  Menú MI CUENTA:
    - Editar Perfil → EditProfileScreen
    - Cuentas Bancarias → BankAccountsScreen
    - Historial → HistoryScreen
    - Mis Reseñas → ReviewsScreen
    - Mis Ofertas → MyOffersScreen  (solo vendor/admin)
  
  Menú SOPORTE:
    - Mis Reclamos → ComplaintsScreen
    - Notificaciones → NotificationsScreen (badge con no leídas)
  
  Menú LEGAL:
    - Términos y Condiciones → TermsScreen
    - Política de Privacidad → PrivacyScreen
    - Acerca de → AboutScreen
    - Centro de Ayuda → HelpScreen
  
  (Admin): solo muestra "Panel de Control" → AdminScreen
  
  Botón Cerrar Sesión → TokenManager.clearSession() → LoginScreen popUpTo(0)

EditProfileScreen
  Campos editables: Nombre completo · Teléfono
  Pre-llena datos actuales
  Botón "Guardar cambios"
```

#### Backend
```
GET /api/v1/users/me
  JWT obligatorio
  Respuesta: user completo con dni, rating, total_transactions, kyc_verified

PATCH /api/v1/users/profile
  Body: { full_name?, phone?, avatar_url? }
  Solo permite esos 3 campos (no email/password)
  Respuesta 200: usuario actualizado

GET /api/v1/notifications/unread-count
  Respuesta: { count: N }
```

---

### FLUJO 12 — Notificaciones

**Pantalla:** `NotificationsScreen`

#### Android
```
NotificationsScreen
  Título con contador de no leídas
  
  Tarjeta de notificación:
    - Fondo azul si no leída, blanco si leída
    - Punto azul indicador no leída
    - Icono tipo-específico:
      * login → CheckCircle verde
      * transaction → SwapHoriz azul
      * voucher → Description amarillo
      * dispute → Gavel naranja
      * offer → Campaign azul claro
      * admin → AdminPanelSettings rojo
      * security → Lock rojo
    - Título (negrita si no leída) + cuerpo
    - Tiempo relativo: "ahora" · "hace 5m" · "hace 2h" · "ayer" · "hace 3d"
  
  Gesto: swipe izquierdo → eliminar (fondo rojo con ícono papelera)
  Tap → navega a recurso (transacción/disputa si tiene resource_id)
  
  Estados: Loading · Error (con retry) · Empty "Todo al día"
```

#### Backend
```
GET /api/v1/notifications
  Query: ?unread=true  (filtra solo no leídas)
  Respuesta: lista ordenada por created_at desc

PATCH /api/v1/notifications/{id}/read
  Marca is_read = True
  Respuesta 200

POST /api/v1/notifications/mark-all-read
  Marca todas del usuario como leídas
  Respuesta 200

DELETE /api/v1/notifications/{id}
  Elimina una notificación
  Respuesta 200

DELETE /api/v1/notifications  (bulk delete)
  Elimina todas del usuario
  Respuesta 200
```

---

### FLUJO 13 — Historial de operaciones

**Pantallas:** `HistoryScreen` → `TransactionScreen` (activas) / `TransactionDetailScreen` (completadas)

#### Android
```
HistoryScreen
  Búsqueda: por ID de transacción o nombre de contraparte
  Filtros chips: Todos · Completados · Pendientes · Disputas
  
  TransactionCard:
    - #TX-{últimos 4} · status badge
    - "Toca para continuar →" si es transacción activa del buyer (naranja)
    - Avatares comprador/vendedor con iniciales
    - Monto en USD
    - Tasa con icono TrendingUp
    - Fecha
  
  Tap en activa → TransactionScreen (continuar operación)
  Tap en completada → TransactionDetailScreen (ver detalle)
  Tap en "Pendientes" del Profile → VendorInboxScreen
```

#### Backend
```
GET /api/v1/transactions
  Devuelve: transacciones como buyer + como vendor, con buyer_name y vendor_name reales
  Query params: ?status=completed|pending|disputed
```

---

## 3. Evaluación UX/UI — Heurísticas de Nielsen

### H1 — Visibilidad del estado del sistema

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 1.1 | `MarketScreen` | Auto-refresh de tasas (8s) y ofertas sin indicador visible para el usuario. La pantalla puede actualizar mientras el usuario está leyendo. | Media |
| 1.2 | `VendorInboxScreen` | Auto-refresh cada 5s sin indicador. Las tarjetas cambian de estado "silenciosamente". | Media |
| 1.3 | `TransactionScreen` | El countdown timer de 15 minutos está solo en el header. Si el usuario baja el scroll, lo pierde de vista. | Alta |
| 1.4 | `ProfileScreen` | No hay estado de carga al obtener datos del usuario. La pantalla aparece vacía un instante. | Baja |
| 1.5 | `BankAccountsScreen` | No hay indicador de carga al agregar/eliminar cuentas (la acción parece instantánea). | Baja |
| 1.6 | `VendorInboxScreen` | Botones de acción (Aceptar, Confirmar) sin spinner propio; difícil saber si el tap registró. | Media |

### H2 — Coincidencia entre el sistema y el mundo real

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 2.1 | `AdminScreen` | Botones "✓ Comprador" / "✓ Vendedor" — no queda claro si significa "dar la razón a" o "asignar como ganador". Debería decir "Resolver a favor de Comprador". | Media |
| 2.2 | `PublishScreen` | El icono de swap (⇄) entre monedas no tiene label. Puede confundir a usuarios nuevos. | Baja |
| 2.3 | `HistoryScreen` | "Toca para continuar →" solo aparece en transacciones activas del buyer, pero la condición no es obvia visualmente. | Baja |

### H3 — Control y libertad del usuario

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 3.1 | `KycScreen` | El botón "←" del KYC navega a MarketScreen (no vuelve al registro). No hay opción de "Completar KYC luego". El usuario queda atrapado en KYC si no tiene cámara disponible. | Alta |
| 3.2 | `RegisterDisputeScreen` | No hay botón "Cancelar" que descarte el formulario de disputa y vuelva a la transacción. Solo "Atrás" del sistema. | Baja |
| 3.3 | `ComplaintsScreen` | No hay forma de ver el detalle de un reclamo ni responder a la nota del admin. Solo status. | Media |
| 3.4 | `TransactionScreen` | El botón "Cancelar" desaparece en estado "voucher_uploaded" — correcto por flujo, pero el usuario no recibe explicación de por qué ya no puede cancelar. | Baja |

### H4 — Consistencia y estándares

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 4.1 | Backend/Android | Estados de transacción inconsistentes: backend usa `"pending_payment"` en algunos lugares, Android espera `"pending"`. Puede causar botones que no aparecen. | Alta |
| 4.2 | `MyOffersScreen` | Los botones "Pausar"/"Reanudar" son outlined pero en otras pantallas las acciones primarias son filled. Confunde jerarquía visual. | Baja |
| 4.3 | `ProfileScreen` | "~1m Respuesta" hardcodeado (no viene del backend). Dato falso que pierde credibilidad. | Media |
| 4.4 | `ProfileScreen` | "100% Completadas" hardcodeado. Mismo problema. | Media |
| 4.5 | Navegación global | En pantallas de detalle (DisputeDetail, TransactionDetail) no hay un botón de "volver" consistente en la TopAppBar — algunas tienen `onBack`, otras dependen del botón físico/gesto. | Baja |

### H5 — Prevención de errores

| # | Pantalla/Backend | Hallazgo | Gravedad |
|---|---|---|---|
| 5.1 | `BankAccountsScreen` | La validación del número de cuenta solo muestra error DESPUÉS de escribir. No hay guía de formato visible antes de empezar (ej: "BCP: 20 dígitos CCI"). | Media |
| 5.2 | `PublishScreen` | No hay feedback de validación inline mientras el usuario escribe el monto o la tasa. El error aparece solo al presionar "Publicar". | Media |
| 5.3 | `RegisterDisputeScreen` | La zona de evidencia aparece como funcional (zona de upload) pero no lo está. Puede hacer que el usuario crea que adjuntó archivos. | Alta |
| 5.4 | `TransactionScreen` | No hay aviso de "transacción expirada" cuando el timer llega a 0. El usuario no sabe qué pasó. | Alta |
| 5.5 | Backend `offers/routes.py` | `== None` en lugar de `is None` para chequeo de max_transaction (línea 121). Puede ignorar el valor en edge cases. | Baja |

### H6 — Reconocimiento en lugar de recuerdo

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 6.1 | `TransactionScreen` | La información bancaria del vendedor (cuenta destino) está en la pantalla, pero si el usuario sale y vuelve, el auto-refresh puede cambiar el estado y ocultar esa sección. | Media |
| 6.2 | `NotificationsScreen` | El gesto swipe-to-dismiss no es descubribleópor el usuario (no hay hint de "desliza para eliminar"). | Media |
| 6.3 | `AdminScreen` | Las pills de estado (arbitraje / revisión / resueltas) no tienen tooltip ni leyenda explicando qué significan. | Baja |

### H7 — Flexibilidad y eficiencia de uso

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 7.1 | `NotificationsScreen` | El botón "Marcar todo como leído" existe en el backend (`POST /mark-all-read`) pero no tiene botón en la UI. El usuario debe leer cada notificación individualmente. | Media |
| 7.2 | `HistoryScreen` | La búsqueda es solo client-side (no hay endpoint de búsqueda en el backend). Si hay muchas transacciones, no funciona con paginación. | Media |
| 7.3 | `MarketScreen` | No hay botón "Actualizar" en el estado de error. Solo el auto-refresh recupera la vista. | Media |

### H8 — Diseño estético y minimalista

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 8.1 | `LoginScreen` | La card de "Demo credentials" expone credenciales de prueba en producción. Debe eliminarse antes de entrega final. | Alta |
| 8.2 | `TransactionScreen` | La pantalla tiene demasiados estados (6+) en un solo Composable con lógica condicional compleja. Dificulta el mantenimiento. | Baja |

### H9 — Ayuda al usuario a reconocer, diagnosticar y recuperarse de errores

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 9.1 | `MarketScreen` | Error de fetch de tasas silencioso (catch vacío en ViewModel). El usuario ve el ticker vacío sin explicación. | Alta |
| 9.2 | `ComplaintsScreen` | No hay confirmación visual tras enviar un reclamo. El formulario simplemente se limpia sin feedback. | Media |
| 9.3 | `RatingScreen` | No hay confirmación visual tras enviar calificación. El flujo navega a Market sin mensaje de éxito. | Baja |
| 9.4 | Backend `disputes/routes.py` | La paginación existe en el backend (page/per_page) pero la UI no implementa "cargar más". Con muchas disputas, el usuario solo ve las primeras 20. | Media |

### H10 — Ayuda y documentación

| # | Pantalla | Hallazgo | Gravedad |
|---|---|---|---|
| 10.1 | `HelpScreen` | Existen botones WhatsApp e Email Intent, pero su contenido no fue revisado en detalle. | Baja |
| 10.2 | `KycScreen` | No hay explicación de cuánto tarda el proceso KYC ni qué pasa si las fotos son rechazadas. | Media |

---

## 4. Problemas encontrados por gravedad

### CRÍTICO — Afectan funcionalidad core

| ID | Descripción | Ubicación | Fix sugerido |
|---|---|---|---|
| C1 | Inconsistencia de estados: `"pending_payment"` (backend) vs `"pending"` (Android). Botones de acción no aparecen. | `transactions/routes.py` vs `TransactionScreen.kt` | Estandarizar a `"pending"` en todo el stack |
| C2 | Zona de evidencia en `RegisterDisputeScreen` aparece funcional pero NO lo está. El usuario cree que adjuntó archivos. | `RegisterDisputeScreen.kt` | Ocultar zona o marcar "próximamente" |
| C3 | No hay mensaje de "transacción expirada" cuando el timer llega a 0. El usuario queda en estado limbo. | `TransactionScreen.kt` | Detectar timer = 0 y mostrar pantalla de expiración |
| C4 | Error silencioso en fetch de tasas en `MarketScreen` (catch vacío). | `MarketViewModel.kt` | Propagar error al `uiState.error` |

### ALTO — Degradan experiencia significativamente

| ID | Descripción | Ubicación | Fix sugerido |
|---|---|---|---|
| A1 | Timer de 15 minutos solo visible en el header. Si el usuario hace scroll, no lo ve. | `TransactionScreen.kt` | Añadir banner sticky o FloatingTimer |
| A2 | `KycScreen` no tiene opción "Completar después". El usuario queda bloqueado si no tiene cámara. | `KycScreen.kt` + `NavGraph.kt` | Añadir botón "Omitir por ahora" que va a MarketScreen |
| A3 | Credenciales hardcodeadas de demo visibles en `LoginScreen` (seguridad + apariencia). | `LoginScreen.kt` | Eliminar o mostrar solo en build debug |
| A4 | "~1m Respuesta" y "100% Completadas" hardcodeados en `ProfileScreen`. | `ProfileScreen.kt` línea 100 | Calcular desde API o eliminar si no hay dato |

### MEDIO — Confunden o frustran al usuario

| ID | Descripción | Ubicación | Fix sugerido |
|---|---|---|---|
| M1 | No hay botón "Marcar todo como leído" en `NotificationsScreen`. El endpoint existe. | `NotificationsScreen.kt` | Añadir botón en TopAppBar |
| M2 | Gesto swipe-to-dismiss en notificaciones no es descubrible. | `NotificationsScreen.kt` | Añadir hint de "Desliza para eliminar" en primera carga |
| M3 | No hay feedback de éxito en `ComplaintsScreen` tras enviar reclamo. | `ComplaintsScreen.kt` | Mostrar Snackbar "Reclamo enviado correctamente" |
| M4 | Búsqueda en `HistoryScreen` es client-side. No funciona con paginación. | `HistoryScreen.kt` | Añadir query param de búsqueda al backend |
| M5 | Formulario de `PublishScreen` sin validación inline. Errores solo al publicar. | `PublishScreen.kt` | Validación en tiempo real por campo |
| M6 | `DisputeDetailScreen` sin estado de carga — aparece vacía un instante. | `DisputeDetailScreen.kt` | Añadir shimmer/loading state |
| M7 | `AdminScreen` botones "✓ Comprador"/"✓ Vendedor" — texto ambiguo. | `AdminScreen.kt` | Cambiar a "Resolver a favor de Comprador" |
| M8 | No hay paginación visible en `MyDisputesScreen` (backend devuelve 20 por página). | `MyDisputesScreen.kt` | Añadir "Cargar más" o lazy scroll |

### BAJO — Mejoras de polish

| ID | Descripción | Ubicación | Fix sugerido |
|---|---|---|---|
| B1 | Icono swap (⇄) en `PublishScreen` sin label. | `PublishScreen.kt` | Añadir tooltip o label "Intercambiar" |
| B2 | `BankAccountsScreen` sin confirmación de eliminación de cuenta. | `BankAccountsScreen.kt` | Dialog de confirmación antes de DELETE |
| B3 | `ReceiptScreen` botón "Descargar Comprobante" sin funcionalidad. | `ReceiptScreen.kt` | Implementar exportar PDF o marcar "próximamente" |
| B4 | Filtros de `MyDisputesScreen` / `HistoryScreen` sin contadores por categoría. | Ambas pantallas | Mostrar "(5)" junto a cada filtro chip |
| B5 | `NotificationsScreen` sin fallback para tiempos muy antiguos (+1 año). | `NotificationsScreen.kt` | Añadir caso "hace más de 1 año" |
| B6 | `ComplaintsScreen` sin vista de detalle — no se puede ver respuesta del admin. | `ComplaintsScreen.kt` | Añadir pantalla detalle de reclamo |

---

## 5. Estado de sprints

| Sprint | Feature | Estado |
|---|---|---|
| 1 | Autenticación + Arquitectura (Login, Register, KYC) | ✅ Completado |
| 2 | Mercado P2P (ofertas, tasas, matching) | ✅ Completado |
| 3 | Flujo Transacción completa (voucher, confirm, receipt) | ✅ Completado |
| 4 | Perfil + Cuentas Bancarias + Reseñas | ✅ Completado |
| 5 | Modo Vendedor + Historial | ✅ Completado |
| 6 | Disputas + Panel Admin | ✅ Completado |
| 7 | Calidad + Extras (Notificaciones, Mis Ofertas, Reclamos) | ⏳ Pendiente |

### Checklist Sprint 7 — Calidad

- [ ] Arreglar estados inconsistentes `"pending_payment"` vs `"pending"` (C1)
- [ ] Ocultar zona de evidencia no funcional en disputas (C2)
- [ ] Añadir pantalla/banner de transacción expirada (C3)
- [ ] Propagar error silencioso de tasas en MarketViewModel (C4)
- [ ] Añadir sticky timer en TransactionScreen (A1)
- [ ] Añadir "Omitir KYC por ahora" (A2)
- [ ] Eliminar credenciales hardcodeadas de LoginScreen en release (A3)
- [ ] Calcular stats reales en ProfileScreen (A4)
- [ ] Botón "Marcar todo como leído" en NotificationsScreen (M1)
- [ ] Feedback de éxito en ComplaintsScreen (M3)
- [ ] Diálogo de confirmación al eliminar cuenta bancaria (B2)
- [ ] Build Android compila sin warnings en release
- [ ] `NetworkSecurityConfig` activo y válido
- [ ] `docker-compose.yml` sin `version:` obsoleto (ya corregido)
- [ ] Migraciones consolidadas a 1 archivo (ya corregido ✅)
- [ ] BuildConfig para URLs (ya corregido ✅)
- [ ] DNI se envía en registro (ya corregido ✅)

---

## 6. Usuarios de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Comprador | `comprador@peruexchange.com` | `Comprador123!` |
| Vendedor | `vendedor@peruexchange.com` | `Vendedor123!` |
| Admin | `admin@peruexchange.com` | `Admin123!` |

---

## 7. Arrancar el proyecto

```bash
# 1. Levantar backend + base de datos en Docker
cd p2p_backend/docker
docker compose up -d --build

# 2. Verificar que el backend responde
curl http://localhost:5000/health

# 3. Android Studio → compilar en modo DEBUG
#    El emulador usa 10.0.2.2:5000 (configurado via BuildConfig.BASE_URL)
#    Para producción: compilar en modo RELEASE (apunta a 157.137.189.178)
```

### Endpoints del backend

| Ambiente | URL |
|---|---|
| Local (Docker) | `http://localhost:5000/api/v1/` |
| Emulador Android | `http://10.0.2.2:5000/api/v1/` |
| Producción (Oracle VM) | `http://157.137.189.178/api/v1/` |

### Cambiar entre local y producción en Android

Editar `p2p_android/gradle.properties`:
```properties
BASE_URL_DEBUG=http://10.0.2.2:5000/api/v1/    # emulador → Docker local
BASE_URL_RELEASE=http://157.137.189.178/api/v1/ # servidor Oracle VM
```
Luego compilar en **Debug** (local) o **Release** (producción). No hay que tocar código.

---

## Estructura del proyecto

```
ProjectP2PFintechforCurrencyExchange/
├── p2p_android/          ← Kotlin + Jetpack Compose
│   ├── gradle.properties         ← URLs del backend (un solo lugar)
│   └── app/src/main/java/com/example/p2p/
│       ├── core/                 (ApiClient, TokenManager, NetworkResult)
│       ├── data/
│       │   ├── remote/api/       (11 interfaces Retrofit)
│       │   ├── remote/model/     (modelos Kotlin)
│       │   └── repository/       (implementaciones)
│       ├── domain/repository/    (interfaces)
│       ├── navigation/           (NavGraph, Screen)
│       ├── presentation/         (27 pantallas + viewmodels)
│       └── ui/theme/             (Color, Theme, Type)
│
├── p2p_backend/          ← Flask + SQLAlchemy + PostgreSQL
│   ├── app/
│   │   ├── api/v1/       (11 blueprints)
│   │   ├── core/         (config, db, security, exceptions, notifications)
│   │   └── models/       (User, Dispute, Complaint)
│   ├── docker/           (Dockerfile×4, docker-compose, nginx)
│   ├── migrations/
│   │   ├── sql/          (12 scripts SQL de init)
│   │   └── versions/     (001_schema_completo.py — 1 sola migración)
│   └── wsgi.py
│
└── PeruExchange_Final.html  ← Demo de referencia (29 pantallas)
```
