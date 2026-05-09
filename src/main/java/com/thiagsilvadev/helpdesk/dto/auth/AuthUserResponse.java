package com.thiagsilvadev.helpdesk.dto.auth;

import com.thiagsilvadev.helpdesk.entity.user.Roles;

public record AuthUserResponse(
        Long id,
        String name,
        Roles role
) {
}
