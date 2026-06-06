-- Tabla: complaints  (depende de: users)
CREATE TABLE IF NOT EXISTS complaints (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(id),
    type        VARCHAR(50) NOT NULL,
    description TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'pending',
    admin_note  TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_complaints_user_id ON complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status  ON complaints(status);
