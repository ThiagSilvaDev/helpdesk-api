package com.thiagsilvadev.helpdesk.entity.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_outbox_events")
public class NotificationOutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationOutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    public NotificationOutboxEvent() {}

    public NotificationOutboxEvent(UUID id, String payload, Instant now) {
        this.id = id;
        this.payload = payload;
        this.status = NotificationOutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = now;
        this.createdAt = now;
    }

    public void markPublished(Instant publishedAt) {
        this.status = NotificationOutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(String errorMessage, Instant failedAt) {
        this.attempts++;
        this.status = NotificationOutboxStatus.FAILED;
        this.lastError = errorMessage;
        long delaySeconds = Math.min(300, (long) Math.pow(2, Math.min(this.attempts, 8)));
        this.nextAttemptAt = failedAt.plusSeconds(delaySeconds);
    }

    public void retry() {
        this.status = NotificationOutboxStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public NotificationOutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
