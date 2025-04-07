--liquibase formatted sql

--changeset yulia:4
CREATE TABLE IF NOT EXISTS cloud.files
(
    id             SERIAL PRIMARY KEY,
    user_id        INTEGER NOT NULL,
    filename       TEXT    NOT NULL,
    filesize_bytes BIGINT  NOT NULL CHECK (filesize_bytes >= 0),
    contents       BYTEA,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES cloud.users (id)
);
