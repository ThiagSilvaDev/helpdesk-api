package com.thiagsilvadev.helpdesk.dto.notification;

import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;

import java.util.UUID;

public record NotificationMessage(
        UUID sourceEventId,
        Long recipientId,
        NotificationType type,
        String title,
        String message,
        Long ticketId,
        Long commentId,
        Long actorUserId
) {
}
