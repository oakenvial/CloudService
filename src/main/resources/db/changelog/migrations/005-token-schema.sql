--liquibase formatted sql

--changeset yulia:5
CREATE TABLE IF NOT EXISTS cloud.tokens
(
    token      TEXT PRIMARY KEY,
    user_id    INTEGER     NOT NULL,
    issued_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES cloud.users (id)
);
