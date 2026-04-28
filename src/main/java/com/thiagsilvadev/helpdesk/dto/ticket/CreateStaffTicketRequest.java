package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStaffTicketRequest(
        @NotBlank @Size(min = 5, max = 100) String title,
        @NotBlank @Size(min = 10) String description,
        @NotNull Long requesterId,
        @NotNull TicketPriority priority
) {
}
