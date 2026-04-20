package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tag values available for a metric")
public record AdminSystemMetricTagResponse(
        @Schema(description = "Tag name", example = "uri")
        String tag,
        @Schema(description = "Available values for the tag")
        List<String> values
) {
}
