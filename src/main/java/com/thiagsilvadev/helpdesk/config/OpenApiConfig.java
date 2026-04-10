package com.thiagsilvadev.helpdesk.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Helpdesk API",
                version = "1.0",
                description = "REST API for support ticket management with JWT authentication and role-based authorization",
                contact = @Contact(name = "ThiagSilvaDev")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT token obtained from POST /api/auth/login"
)
public class OpenApiConfig {
}
