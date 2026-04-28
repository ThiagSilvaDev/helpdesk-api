package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketPriorityRequest(
        @NotNull(message = "Priority is required")
        TicketPriority priority
) {
}
