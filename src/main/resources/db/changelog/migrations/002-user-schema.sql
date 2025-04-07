--liquibase formatted sql

--changeset yulia:2
CREATE TABLE IF NOT EXISTS cloud.users
(
    id         SERIAL PRIMARY KEY,
    login      TEXT NOT NULL,
    password   TEXT NOT NULL,
    first_name TEXT,
    last_name  TEXT,
    email      TEXT
);
