package com.thiagsilvadev.helpdesk.dto.adminsystem;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminSystemHealthResponse(
        String status,
        @ArraySchema(schema = @Schema(implementation = AdminSystemHealthComponentResponse.class))
        List<AdminSystemHealthComponentResponse> components
) {
}
