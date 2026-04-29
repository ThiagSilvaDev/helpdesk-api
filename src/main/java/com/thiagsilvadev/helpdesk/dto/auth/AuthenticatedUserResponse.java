package com.thiagsilvadev.helpdesk.dto.auth;

import com.thiagsilvadev.helpdesk.entity.Roles;
public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        Roles role,
        boolean active
) {
}
