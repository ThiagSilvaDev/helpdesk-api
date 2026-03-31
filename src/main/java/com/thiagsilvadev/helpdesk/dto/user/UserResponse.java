package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;

public record UserResponse(
        Long id,
        String name,
        String email,
        Roles role,
        boolean active
) {
}
