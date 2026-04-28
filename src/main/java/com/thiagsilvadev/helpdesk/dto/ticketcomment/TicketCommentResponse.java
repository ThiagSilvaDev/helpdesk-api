package com.thiagsilvadev.helpdesk.dto.ticketcomment;

import java.time.Instant;

public record TicketCommentResponse(
        Long id,
        Long ticketId,
        TicketCommentAuthorInfo author,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
