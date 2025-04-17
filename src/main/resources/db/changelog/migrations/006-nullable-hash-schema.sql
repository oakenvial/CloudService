--liquibase formatted sql

--changeset yulia:6
--comment: allow NULL values in the hash column
ALTER TABLE cloud.files
    ALTER COLUMN hash DROP NOT NULL;
