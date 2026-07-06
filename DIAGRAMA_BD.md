erDiagram
    users ||--o{ bank_accounts : "registra"
    users ||--o{ offers : "publica (vendor)"
    offers ||--o{ transactions : "genera"
    users ||--o{ transactions : "compra (buyer)"
    users ||--o{ transactions : "vende (vendor)"
    transactions ||--o{ vouchers : "tiene comprobantes"
    users ||--o{ vouchers : "sube (sender)"
    transactions ||--o{ ratings : "recibe"
    users ||--o{ ratings : "califica (rater)"
    users ||--o{ ratings : "es calificado (ratee)"
    transactions ||--o{ disputes : "puede disputarse"
    users ||--o{ disputes : "abre (initiator)"
    users ||--o{ disputes : "resuelve (admin)"
    users ||--o{ complaints : "presenta"
    users ||--o{ notifications : "recibe"
    users ||--o{ audit_logs : "genera"
    users {
        varchar id PK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar dni UK
        varchar phone
        varchar avatar_url
        varchar signature_url
        varchar dni_image_url
        varchar dni_back_url
        varchar selfie_url
        varchar role
        varchar kyc_status
        boolean kyc_verified
        boolean terms_accepted
        varchar terms_url
        varchar terms_version
        timestamp terms_accepted_at
        float rating
        integer total_transactions
        boolean is_active
        boolean is_banned
        text ban_reason
        varchar fcm_token
        timestamp created_at
        timestamp updated_at
    }
    currencies {
        varchar id PK
        varchar code UK
        varchar name
        varchar symbol
        timestamp created_at
        timestamp updated_at
    }
    exchange_rates {
        varchar id PK
        varchar from_currency
        varchar to_currency
        float rate
        timestamp created_at
        timestamp updated_at
    }
    bank_accounts {
        varchar id PK
        varchar user_id FK
        varchar bank_name
        varchar account_number
        varchar account_holder
        varchar account_type
        varchar currency
        boolean is_primary
        boolean is_verified
        timestamp created_at
        timestamp updated_at
    }
    offers {
        varchar id PK
        varchar vendor_id FK
        varchar from_currency
        varchar to_currency
        float amount
        float available_amount
        float price_per_unit
        varchar offer_type
        varchar status
        float min_transaction
        float max_transaction
        text payment_methods
        timestamp created_at
        timestamp updated_at
    }
    transactions {
        varchar id PK
        varchar offer_id FK
        varchar buyer_id FK
        varchar vendor_id FK
        float amount_from
        float amount_to
        float exchange_rate
        varchar status
        text buyer_payment_account
        text vendor_payment_account
        text vendor_voucher_url
        text receipt_pdf_url
        timestamp accepted_at
        timestamp confirmed_at
        timestamp created_at
        timestamp updated_at
    }
    vouchers {
        varchar id PK
        varchar transaction_id FK
        varchar sender_id FK
        varchar image_url
        text description
        varchar status
        timestamp created_at
        timestamp updated_at
    }
    ratings {
        varchar id PK
        varchar transaction_id FK
        varchar rater_id FK
        varchar ratee_id FK
        integer score
        text comment
        timestamp created_at
        timestamp updated_at
    }
    disputes {
        varchar id PK
        varchar transaction_id FK
        varchar initiator_id FK
        varchar reason
        text description
        varchar evidence_url
        varchar status
        varchar resolved_by FK
        varchar resolution
        text resolution_note
        timestamp resolved_at
        timestamp created_at
        timestamp updated_at
    }
    complaints {
        varchar id PK
        varchar user_id FK
        varchar type
        text description
        varchar status
        text admin_note
        timestamp created_at
        timestamp updated_at
    }
    notifications {
        varchar id PK
        varchar user_id FK
        varchar type
        varchar title
        text body
        boolean is_read
        varchar resource_id
        timestamp created_at
        timestamp updated_at
    }
    audit_logs {
        varchar id PK
        varchar user_id FK
        varchar action
        varchar resource
        text changes
        timestamp created_at
        timestamp updated_at
    }