package com.thiagsilvadev.helpdesk.entity.notification;

import com.thiagsilvadev.helpdesk.entity.AuditableEntity;
import com.thiagsilvadev.helpdesk.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false, updatable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80, updatable = false)
    private NotificationType type;

    @Column(nullable = false, updatable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String message;

    @Column(updatable = false)
    private Long ticketId;

    @Column(updatable = false)
    private Long commentId;

    @Column(updatable = false)
    private Long actorUserId;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID sourceEventId;

    private Instant readAt;

    public Notification() {}

    public Notification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            Long ticketId,
            Long commentId,
            Long actorUserId,
            UUID sourceEventId) {
        this.recipient = Objects.requireNonNull(recipient, "recipient must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.title = requireText(title, "title");
        this.message = requireText(message, "message");
        this.ticketId = ticketId;
        this.commentId = commentId;
        this.actorUserId = actorUserId;
        this.sourceEventId = Objects.requireNonNull(sourceEventId, "sourceEventId must not be null");
    }

    public void markAsRead(Instant readAt) {
        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value.strip();
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
