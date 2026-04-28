package com.thiagsilvadev.helpdesk.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available ticket statuses in the helpdesk", enumAsRef = true)
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED,
    CANCELLED
}
