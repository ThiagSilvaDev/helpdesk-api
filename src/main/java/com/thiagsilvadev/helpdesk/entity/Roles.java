package com.thiagsilvadev.helpdesk.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available user roles in the helpdesk", enumAsRef = true)
public enum Roles {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_TECHNICIAN
}
