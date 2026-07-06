# Base de Datos — PeruExchange P2P
---

## Relaciones (resumen)

```
users ──< bank_accounts
users ──< offers ──< transactions >── users (buyer / vendor)
transactions ──< vouchers
transactions ──< ratings
transactions ──< disputes
users ──< complaints
users ──< notifications
users ──< audit_logs
```

---

## 1. `users`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| email | VARCHAR(120) | NO | — | UNIQUE, INDEX |
| password_hash | VARCHAR(255) | NO | — | |
| full_name | VARCHAR(255) | NO | — | |
| dni | VARCHAR(20) | SÍ | — | UNIQUE |
| phone | VARCHAR(20) | SÍ | — | |
| avatar_url | VARCHAR(500) | SÍ | — | |
| signature_url | VARCHAR(500) | SÍ | — | firma (KYC) |
| dni_image_url | VARCHAR(500) | SÍ | — | DNI frontal (KYC) |
| dni_back_url | VARCHAR(500) | SÍ | — | DNI reverso (KYC) |
| selfie_url | VARCHAR(500) | SÍ | — | selfie (KYC) |
| role | VARCHAR(20) | SÍ | `'buyer'` | `buyer` \| `vendor` \| `admin` |
| kyc_status | VARCHAR(20) | NO | `'approved'` | *(migración 002)* |
| kyc_verified | BOOLEAN | SÍ | `TRUE` | |
| terms_accepted | BOOLEAN | NO | `FALSE` | *(migración 002)* |
| terms_url | VARCHAR(500) | SÍ | — | *(migración 002)* PDF de términos firmado |
| terms_version | VARCHAR(50) | SÍ | — | *(migración 002)* |
| terms_accepted_at | TIMESTAMP | SÍ | — | *(migración 002)* |
| rating | FLOAT | SÍ | `0.0` | promedio de calificaciones |
| total_transactions | INTEGER | SÍ | `0` | |
| is_active | BOOLEAN | SÍ | `TRUE` | |
| is_banned | BOOLEAN | SÍ | `FALSE` | |
| ban_reason | TEXT | SÍ | — | |
| fcm_token | VARCHAR(500) | SÍ | — | token push Firebase |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_users_email (email)`

---

## 2. `currencies`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| code | VARCHAR(10) | NO | — | UNIQUE (ej. PEN, USD) |
| name | VARCHAR(100) | NO | — | |
| symbol | VARCHAR(10) | NO | — | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

---

## 3. `exchange_rates`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| from_currency | VARCHAR(10) | NO | — | |
| to_currency | VARCHAR(10) | NO | — | |
| rate | FLOAT | NO | — | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

---

## 4. `bank_accounts`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| user_id | VARCHAR(36) | NO | — | FK → `users(id)` ON DELETE CASCADE |
| bank_name | VARCHAR(100) | NO | — | BCP, Interbank, BBVA, Yape, Plin, otros |
| account_number | VARCHAR(50) | NO | — | CCI 20 díg. (bancos) / celular 9 díg. (Yape/Plin) |
| account_holder | VARCHAR(255) | NO | — | titular |
| account_type | VARCHAR(20) | NO | — | `savings` \| `checking` |
| currency | VARCHAR(10) | NO | — | |
| is_primary | BOOLEAN | SÍ | `FALSE` | cuenta principal |
| is_verified | BOOLEAN | SÍ | `FALSE` | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_bank_accounts_user_id (user_id)`

---

## 5. `offers`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| vendor_id | VARCHAR(36) | NO | — | FK → `users(id)` ON DELETE CASCADE |
| from_currency | VARCHAR(10) | NO | — | moneda que vende |
| to_currency | VARCHAR(10) | NO | — | moneda que recibe |
| amount | FLOAT | NO | — | monto total publicado |
| available_amount | FLOAT | NO | — | saldo disponible (se descuenta por compra) |
| price_per_unit | FLOAT | NO | — | tipo de cambio de la oferta |
| offer_type | VARCHAR(20) | NO | — | `sell` \| `buy` |
| status | VARCHAR(20) | SÍ | `'active'` | `active` \| `paused` \| `closed` |
| min_transaction | FLOAT | SÍ | `0` | |
| max_transaction | FLOAT | SÍ | — | |
| payment_methods | TEXT | SÍ | — | métodos de pago aceptados |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_offers_vendor_id (vendor_id)`, `idx_offers_status (status)`

---

## 6. `transactions`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| offer_id | VARCHAR(36) | NO | — | FK → `offers(id)` |
| buyer_id | VARCHAR(36) | NO | — | FK → `users(id)` ON DELETE CASCADE |
| vendor_id | VARCHAR(36) | NO | — | FK → `users(id)` ON DELETE CASCADE |
| amount_from | FLOAT | NO | — | monto en moneda origen |
| amount_to | FLOAT | NO | — | monto en moneda destino |
| exchange_rate | FLOAT | NO | — | |
| status | VARCHAR(40) | SÍ | `'pending'` | `pending` \| `accepted` \| `voucher_uploaded` \| `completed` \| `closed` \| `cancelled` \| `disputed` \| `paused` |
| buyer_payment_account | TEXT | SÍ | — | |
| vendor_payment_account | TEXT | SÍ | — | |
| vendor_voucher_url | TEXT | SÍ | — | comprobante del vendedor |
| receipt_pdf_url | TEXT | SÍ | — | PDF del recibo (al completar) |
| accepted_at | TIMESTAMP | SÍ | — | *(migración 002)* |
| confirmed_at | TIMESTAMP | SÍ | — | *(migración 002)* |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_transactions_buyer_id (buyer_id)`, `idx_transactions_vendor_id (vendor_id)`, `idx_transactions_status (status)`

---

## 7. `vouchers`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| transaction_id | VARCHAR(36) | NO | — | FK → `transactions(id)` |
| sender_id | VARCHAR(36) | NO | — | FK → `users(id)` (quién lo subió) |
| image_url | VARCHAR(500) | NO | — | imagen en Supabase Storage |
| description | TEXT | SÍ | — | |
| status | VARCHAR(40) | SÍ | `'pending'` | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

---

## 8. `ratings`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| transaction_id | VARCHAR(36) | NO | — | FK → `transactions(id)` |
| rater_id | VARCHAR(36) | NO | — | FK → `users(id)` (quién califica) |
| ratee_id | VARCHAR(36) | NO | — | FK → `users(id)` (calificado) |
| score | INTEGER | NO | — | 1 a 5 |
| comment | TEXT | SÍ | — | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_ratings_rater_id (rater_id)`, `idx_ratings_ratee_id (ratee_id)`

---

## 9. `disputes`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| transaction_id | VARCHAR(36) | NO | — | FK → `transactions(id)` |
| initiator_id | VARCHAR(36) | NO | — | FK → `users(id)` (quién abre la disputa) |
| reason | VARCHAR(255) | NO | — | `payment_not_received` \| `wrong_amount` \| `voucher_fake` \| `no_response` \| `other` |
| description | TEXT | SÍ | — | |
| evidence_url | VARCHAR(500) | SÍ | — | *(migración 003)* imagen de evidencia |
| status | VARCHAR(20) | SÍ | `'open'` | `open` \| `under_review` \| `resolved` \| `closed` |
| resolved_by | VARCHAR(36) | SÍ | — | FK → `users(id)` (admin que resolvió) |
| resolution | VARCHAR(20) | SÍ | — | `favour_buyer` \| `favour_vendor` |
| resolution_note | TEXT | SÍ | — | nota del admin |
| resolved_at | TIMESTAMP | SÍ | — | |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_disputes_transaction_id (transaction_id)`, `idx_disputes_status (status)`

---

## 10. `audit_logs`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| user_id | VARCHAR(36) | SÍ | — | FK → `users(id)` |
| action | VARCHAR(100) | NO | — | ej. `resolve_complaint`, `ban_user` |
| resource | VARCHAR(100) | NO | — | ej. `complaint:<id>` |
| changes | TEXT | SÍ | — | JSON con el detalle del cambio |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_audit_logs_user_id (user_id)`

---

## 11. `complaints`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| user_id | VARCHAR(36) | NO | — | FK → `users(id)` |
| type | VARCHAR(50) | NO | — | `transaction_issue` \| `platform_error` \| `payment_issue` \| `account_issue` \| `other` |
| description | TEXT | NO | — | |
| status | VARCHAR(20) | NO | `'pending'` | `pending` \| `under_review` \| `resolved` \| `closed` |
| admin_note | TEXT | SÍ | — | respuesta del administrador |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `idx_complaints_user_id (user_id)`, `idx_complaints_status (status)`

---

## 12. `notifications`

| Columna | Tipo | Nulo | Default | Notas |
|---|---|---|---|---|
| id | VARCHAR(36) | NO | uuid4 | PK |
| user_id | VARCHAR(36) | NO | — | FK → `users(id)` (destinatario) |
| type | VARCHAR(50) | NO | — | `login` \| `transaction` \| `voucher` \| `dispute` \| `offer` \| `complaint` \| `admin` \| `security` \| `kyc` |
| title | VARCHAR(255) | NO | — | |
| body | TEXT | NO | — | |
| is_read | BOOLEAN | NO | `FALSE` | |
| resource_id | VARCHAR(36) | SÍ | — | id del recurso asociado (transacción, disputa, reclamo) — usado para navegar al tocar |
| created_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |
| updated_at | TIMESTAMP | NO | `CURRENT_TIMESTAMP` | |

Índices: `ix_notifications_user_id (user_id)`, `ix_notifications_is_read (is_read)`
