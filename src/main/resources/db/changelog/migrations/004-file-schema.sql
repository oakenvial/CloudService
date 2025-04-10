--liquibase formatted sql

--changeset yulia:4
CREATE TABLE IF NOT EXISTS cloud.files
(
    id             SERIAL PRIMARY KEY,
    user_id        INTEGER     NOT NULL,
    filename       TEXT        NOT NULL,
    filesize_bytes BIGINT      NOT NULL CHECK (filesize_bytes >= 0),
    hash           TEXT        NOT NULL,
    s3_link        TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES cloud.users (id)
);

CREATE INDEX idx_files_user_id ON cloud.files (user_id);
CREATE INDEX idx_files_filename ON cloud.files (filename);
