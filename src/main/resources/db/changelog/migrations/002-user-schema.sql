--liquibase formatted sql

--changeset yulia:2
CREATE TABLE IF NOT EXISTS cloud.users
(
    id         SERIAL PRIMARY KEY,
    username   TEXT NOT NULL,
    password   TEXT NOT NULL,
    first_name TEXT,
    last_name  TEXT,
    email      TEXT
);

CREATE INDEX idx_users_username ON cloud.users (username);
CREATE INDEX idx_users_first_last ON cloud.users (first_name, last_name);
