package com.thiagsilvadev.helpdesk.dto.ticket;

public record UpdateTicketRequest(
        String title,
        String description
) {
}

