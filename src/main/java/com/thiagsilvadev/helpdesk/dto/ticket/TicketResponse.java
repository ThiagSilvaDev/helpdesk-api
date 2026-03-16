package com.thiagsilvadev.helpdesk.dto.ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String status,
        UserInfo client,
        UserInfo technician,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {
    public record UserInfo(Long id, String name) {}
}