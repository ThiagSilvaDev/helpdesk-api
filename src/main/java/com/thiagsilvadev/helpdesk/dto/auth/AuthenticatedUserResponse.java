package com.thiagsilvadev.helpdesk.dto.auth;

import com.thiagsilvadev.helpdesk.entity.user.Roles;
public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        Roles role,
        boolean active
) {
}
