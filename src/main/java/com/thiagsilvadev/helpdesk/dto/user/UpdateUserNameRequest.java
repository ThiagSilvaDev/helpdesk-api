package com.thiagsilvadev.helpdesk.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserNameRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String name
) {
}
