package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Catalog of metric names exposed by the admin system API")
public record AdminSystemMetricNamesResponse(
        @Schema(description = "Sorted metric names exposed by the backend")
        List<String> names
) {
}
