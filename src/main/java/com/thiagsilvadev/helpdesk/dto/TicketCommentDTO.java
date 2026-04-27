package com.thiagsilvadev.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public interface TicketCommentDTO {

    record Response(
            Long id,
            Long ticketId,
            AuthorInfo author,
            String content,
            Instant createdAt,
            Instant updatedAt
    ) {
        public record AuthorInfo(Long id, String name) {
        }
    }

    interface Create {
        record Request(
                @NotBlank
                @Size(max = 5000)
                String content
        ) {
        }
    }

    interface Update {
        record Request(
                @NotBlank
                @Size(max = 5000)
                String content
        ) {
        }
    }
}
