package com.thiagsilvadev.helpdesk.dto.ticket;

import jakarta.validation.constraints.NotNull;

public record AssignTechnicianRequest(
        @NotNull(message = "Technician ID is required")
        Long technicianId
) {
}
