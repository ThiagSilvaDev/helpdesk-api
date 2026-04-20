package com.thiagsilvadev.helpdesk.dto;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class TicketDto {

    private TicketDto() {
    }

    public record StaffCreateTicketRequest(
            @NotBlank @Size(min = 5, max = 100) String title,
            @NotBlank @Size(min = 10) String description,
            @NotNull Long requesterId,
            @NotNull TicketPriority priority
    ) {
    }

    public record UserCreateTicketRequest(
            @NotBlank @Size(min = 5, max = 100) String title,
            @NotBlank @Size(min = 10) String description
    ) {
    }

    public record AssignTechnicianRequest(Long technicianId) {
    }

    public record UpdateTicketRequest(
            @NotBlank
            @Size(min = 5, max = 100)
            String title,

            @NotBlank
            @Size(min = 10)
            String description
    ) {
    }

    public record UpdatePriorityRequest(
            @NotNull(message = "Priority is required")
            TicketPriority priority
    ) {
    }

    public record TicketSearchCriteria(
            TicketStatus status,
            TicketPriority priority
    ) {
    }

    public record TicketResponse(
            Long id,
            String title,
            String description,
            TicketStatus status,
            TicketPriority priority,
            UserInfo client,
            UserInfo technician,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt
    ) {
        public record UserInfo(Long id, String name) {
        }
    }
}
