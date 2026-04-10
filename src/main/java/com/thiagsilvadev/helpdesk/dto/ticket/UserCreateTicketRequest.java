package com.thiagsilvadev.helpdesk.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateTicketRequest(
        @NotBlank @Size(min = 5, max = 100) String title,
        @NotBlank @Size(min = 10) String description
) {
}
