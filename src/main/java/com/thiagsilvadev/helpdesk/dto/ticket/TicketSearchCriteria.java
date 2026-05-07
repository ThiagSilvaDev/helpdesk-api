package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketStatus;

public record TicketSearchCriteria(
        TicketStatus status,
        TicketPriority priority
) {
}
