-- Tabla: users
CREATE TABLE IF NOT EXISTS users (
    id               VARCHAR(36)  PRIMARY KEY,
    email            VARCHAR(120) UNIQUE NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    full_name        VARCHAR(255) NOT NULL,
    dni              VARCHAR(20)  UNIQUE,
    phone            VARCHAR(20),
    avatar_url       VARCHAR(500),
    signature_url    VARCHAR(500),
    dni_image_url    VARCHAR(500),
    role             VARCHAR(20)  DEFAULT 'buyer',
    kyc_verified     BOOLEAN      DEFAULT FALSE,
    rating           FLOAT        DEFAULT 0.0,
    total_transactions INTEGER    DEFAULT 0,
    is_active        BOOLEAN      DEFAULT TRUE,
    is_banned        BOOLEAN      DEFAULT FALSE,
    ban_reason       TEXT,
    fcm_token        VARCHAR(500),
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
