-- Tabla: audit_logs  (depende de: users)
CREATE TABLE IF NOT EXISTS audit_logs (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  REFERENCES users(id),
    action     VARCHAR(100) NOT NULL,
    resource   VARCHAR(100) NOT NULL,
    changes    TEXT,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
