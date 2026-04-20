package com.thiagsilvadev.helpdesk.dto;

import com.thiagsilvadev.helpdesk.entity.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UserDto {

    private UserDto() {
    }

    public record CreateUserRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
            String password,

            @NotNull(message = "Role is required")
            Roles role
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank
            @Size(min = 3, max = 50)
            String name,

            @NotBlank
            @Email
            String email,

            @NotNull
            Roles role
    ) {
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            Roles role,
            boolean active
    ) {
    }
}
