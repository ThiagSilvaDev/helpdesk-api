package com.thiagsilvadev.helpdesk.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, message = "Description must be at least 10 characters")
        String description
) {
}

