package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Health summary returned by the admin system API")
public record AdminSystemHealthResponse(
        @Schema(description = "Overall system health status", example = "UP")
        String status,
        @Schema(description = "Top-level health contributors returned by Spring Boot health")
        List<AdminSystemHealthComponentResponse> components
) {
}
