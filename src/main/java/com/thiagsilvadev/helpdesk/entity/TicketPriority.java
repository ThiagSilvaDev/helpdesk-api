package com.thiagsilvadev.helpdesk.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available ticket priorities in the helpdesk", enumAsRef = true)
public enum TicketPriority {
    TRIAGE,
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
