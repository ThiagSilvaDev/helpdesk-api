package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        TicketUserInfo client,
        TicketUserInfo technician,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
}
