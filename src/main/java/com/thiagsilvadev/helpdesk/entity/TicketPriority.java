package com.thiagsilvadev.helpdesk.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketPriority {
    TRIAGE,
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
