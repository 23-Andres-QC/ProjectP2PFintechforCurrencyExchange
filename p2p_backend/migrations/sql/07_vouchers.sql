-- Tabla: vouchers  (depende de: transactions, users)
CREATE TABLE IF NOT EXISTS vouchers (
    id             VARCHAR(36)  PRIMARY KEY,
    transaction_id VARCHAR(36)  NOT NULL REFERENCES transactions(id),
    sender_id      VARCHAR(36)  NOT NULL REFERENCES users(id),
    image_url      VARCHAR(500) NOT NULL,
    description    TEXT,
    status         VARCHAR(40)  DEFAULT 'pending',
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
