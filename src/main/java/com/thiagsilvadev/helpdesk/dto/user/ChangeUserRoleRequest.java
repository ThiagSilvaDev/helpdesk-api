package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull(message = "Role is required")
        Roles role
) {
}
