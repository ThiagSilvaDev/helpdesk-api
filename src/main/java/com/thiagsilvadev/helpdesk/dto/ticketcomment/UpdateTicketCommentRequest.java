package com.thiagsilvadev.helpdesk.dto.ticketcomment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTicketCommentRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {
}
