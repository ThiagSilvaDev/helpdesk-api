package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketStatus;
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
        Instant closedAt) {}
