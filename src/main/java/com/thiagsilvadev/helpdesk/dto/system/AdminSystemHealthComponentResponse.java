package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a single health contributor exposed by the admin system API")
public record AdminSystemHealthComponentResponse(
        @Schema(description = "Contributor name", example = "db")
        String name,
        @Schema(description = "Contributor status", example = "UP")
        String status
) {
}
