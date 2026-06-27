-- Tabla: ratings  (depende de: transactions, users)
CREATE TABLE IF NOT EXISTS ratings (
    id             VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL REFERENCES transactions(id),
    rater_id       VARCHAR(36) NOT NULL REFERENCES users(id),
    ratee_id       VARCHAR(36) NOT NULL REFERENCES users(id),
    score          INTEGER     NOT NULL,
    comment        TEXT,
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ratings_rater_id ON ratings(rater_id);
CREATE INDEX IF NOT EXISTS idx_ratings_ratee_id ON ratings(ratee_id);
