package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;

public record TicketSearchCriteria (
        TicketStatus status,
        TicketPriority priority
) {}
