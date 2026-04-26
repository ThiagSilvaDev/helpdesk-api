-- Helpdesk initial schema
-- V1: Users and Tickets tables

CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role       VARCHAR(50) NOT NULL,
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE tickets
(
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT         NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    priority      VARCHAR(50)  NOT NULL,
    client_id     BIGINT       NOT NULL REFERENCES users (id),
    technician_id BIGINT REFERENCES users (id),
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    closed_at     TIMESTAMPTZ
);

-- Indexes for common query patterns
CREATE INDEX idx_tickets_client_id ON tickets (client_id);
CREATE INDEX idx_tickets_technician_id ON tickets (technician_id);
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_priority ON tickets (priority);
CREATE INDEX idx_users_email ON users (email);
