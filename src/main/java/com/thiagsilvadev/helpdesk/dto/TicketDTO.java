package com.thiagsilvadev.helpdesk.dto;

import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public interface TicketDTO {

    @Schema(name = "TicketResponse")
    record Response(
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

    interface Create {
        record UserRequest(
                @NotBlank @Size(min = 5, max = 100) String title,
                @NotBlank @Size(min = 10) String description
        ) {
        }

        record StaffRequest(
                @NotBlank @Size(min = 5, max = 100) String title,
                @NotBlank @Size(min = 10) String description,
                @NotNull Long requesterId,
                @NotNull TicketPriority priority
        ) {
        }
    }

    interface AssignTechnician {
        record Request(Long technicianId) {
        }
    }

    interface Update {
        record Request(
                @NotBlank
                @Size(min = 5, max = 100)
                String title,

                @NotBlank
                @Size(min = 10)
                String description
        ) {
        }
    }

    interface UpdatePriority {
        record Request(
                @NotNull(message = "Priority is required")
                TicketPriority priority
        ) {
        }
    }

    interface Search {
        record Criteria(
                TicketStatus status,
                TicketPriority priority
        ) {
        }
    }
}
