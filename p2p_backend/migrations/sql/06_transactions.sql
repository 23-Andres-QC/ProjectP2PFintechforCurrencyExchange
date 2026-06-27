-- Tabla: transactions  (depende de: users, offers)
CREATE TABLE IF NOT EXISTS transactions (
    id                     VARCHAR(36) PRIMARY KEY,
    offer_id               VARCHAR(36) NOT NULL REFERENCES offers(id),
    buyer_id               VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vendor_id              VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount_from            FLOAT       NOT NULL,
    amount_to              FLOAT       NOT NULL,
    exchange_rate          FLOAT       NOT NULL,
    status                 VARCHAR(40) DEFAULT 'pending',
    buyer_payment_account  TEXT,
    vendor_payment_account TEXT,
    vendor_voucher_url     TEXT,
    receipt_pdf_url        TEXT,
    created_at             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_buyer_id  ON transactions(buyer_id);
CREATE INDEX IF NOT EXISTS idx_transactions_vendor_id ON transactions(vendor_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status    ON transactions(status);
