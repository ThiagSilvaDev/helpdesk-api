package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotNull(message = "Role is required")
        Roles role
) {
}

