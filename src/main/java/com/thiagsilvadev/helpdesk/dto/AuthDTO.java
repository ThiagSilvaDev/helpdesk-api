package com.thiagsilvadev.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface AuthDTO {

    record Response(String token) {
    }

    interface Login {
        record Request(
                @NotBlank(message = "Email is required")
                @Email(message = "Email format is invalid")
                String email,

                @NotBlank(message = "Password is required")
                String password
        ) {
        }
    }
}
