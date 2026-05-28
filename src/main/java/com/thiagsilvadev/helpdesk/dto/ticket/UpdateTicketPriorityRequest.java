package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketPriorityRequest(@NotNull(message = "Priority is required") TicketPriority priority) {}
