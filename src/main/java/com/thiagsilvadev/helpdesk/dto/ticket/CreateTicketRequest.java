package com.thiagsilvadev.helpdesk.dto.ticket;

public record CreateTicketRequest(
        String title,
        String description,
        Long clientId
) {
}

