package com.thiagsilvadev.helpdesk.dto.notification;

import com.thiagsilvadev.helpdesk.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long ticketId,
        Long commentId,
        Long actorUserId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
