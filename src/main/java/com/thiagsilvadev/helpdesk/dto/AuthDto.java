package com.thiagsilvadev.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record AuthResponse(String token) {
    }
}
