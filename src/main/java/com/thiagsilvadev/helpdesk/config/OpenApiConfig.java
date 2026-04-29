package com.thiagsilvadev.helpdesk.config;

import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }

    @Bean
    public OpenAPI helpdeskOpenApi() {
        Components components = new Components()
                .addSchemas("ProblemDetail", buildProblemDetailSchema())
                .addResponses("BadRequest", problemResponse("Bad request"))
                .addResponses("Unauthorized", problemResponse("Unauthorized"))
                .addResponses("Forbidden", problemResponse("Access denied"))
                .addResponses("NotFound", problemResponse("Resource not found"))
                .addResponses("Conflict", problemResponse("Conflict"))
                .addResponses("UnprocessableEntity", problemResponse("Unprocessable entity"));

        return new OpenAPI().components(components);
    }

    private ApiResponse problemResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))
                ));
    }

    private Schema<?> buildProblemDetailSchema() {
        return new ObjectSchema()
                .addRequiredItem("title")
                .addRequiredItem("status")
                .addProperty("type", new StringSchema().format("uri"))
                .addProperty("title", new StringSchema())
                .addProperty("status", new IntegerSchema())
                .addProperty("detail", new StringSchema())
                .addProperty("instance", new StringSchema().format("uri"));
    }
}
