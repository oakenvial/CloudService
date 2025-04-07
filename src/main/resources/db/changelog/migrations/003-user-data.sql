--liquibase formatted sql

--changeset yulia:3
--all passwords are "password" for testing purposes
INSERT INTO cloud.users (login, password, first_name, last_name, email)
VALUES
    ('jdoe', '$2a$10$CdZZ6SjpkmHOK0FD.1XzBu.P8Y2d2cblbno3svXIOx1PdQ5jmrZm.', 'John', 'Doe', 'jdoe@example.com'),
    ('asmith', '$2a$10$CdZZ6SjpkmHOK0FD.1XzBu.P8Y2d2cblbno3svXIOx1PdQ5jmrZm.', 'Alice', 'Smith', 'asmith@example.com'),
    ('bjones', '$2a$10$CdZZ6SjpkmHOK0FD.1XzBu.P8Y2d2cblbno3svXIOx1PdQ5jmrZm.', 'Bob', 'Jones', 'bjones@example.com'),
    ('slee', '$2a$10$CdZZ6SjpkmHOK0FD.1XzBu.P8Y2d2cblbno3svXIOx1PdQ5jmrZm.', 'Susan', 'Lee', 'slee@example.com'),
    ('kmiller', '$2a$10$CdZZ6SjpkmHOK0FD.1XzBu.P8Y2d2cblbno3svXIOx1PdQ5jmrZm.', 'Karen', 'Miller', 'kmiller@example.com');
