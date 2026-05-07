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

CREATE TABLE ticket_comments
(
    id         BIGSERIAL PRIMARY KEY,
    ticket_id  BIGINT      NOT NULL REFERENCES tickets (id),
    author_id  BIGINT      NOT NULL REFERENCES users (id),
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE notifications
(
    id              BIGSERIAL PRIMARY KEY,
    recipient_id    BIGINT       NOT NULL REFERENCES users (id),
    type            VARCHAR(80)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,
    ticket_id       BIGINT REFERENCES tickets (id),
    comment_id      BIGINT REFERENCES ticket_comments (id) ON DELETE SET NULL,
    actor_user_id   BIGINT REFERENCES users (id),
    source_event_id UUID         NOT NULL UNIQUE,
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);

CREATE TABLE notification_outbox_events
(
    id              UUID PRIMARY KEY,
    payload         TEXT        NOT NULL,
    status          VARCHAR(30) NOT NULL,
    attempts        INTEGER     NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    last_error      TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ
);

-- Indexes for common query patterns
CREATE INDEX idx_tickets_client_id ON tickets (client_id);
CREATE INDEX idx_tickets_technician_id ON tickets (technician_id);
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_priority ON tickets (priority);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments (ticket_id);
CREATE INDEX idx_ticket_comments_author_id ON ticket_comments (author_id);
CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_read ON notifications (recipient_id, read_at);
CREATE INDEX idx_notification_outbox_status_next_attempt ON notification_outbox_events (status, next_attempt_at);
