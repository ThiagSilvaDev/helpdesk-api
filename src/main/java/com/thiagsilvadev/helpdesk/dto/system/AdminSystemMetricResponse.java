package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detailed actuator metric returned by the admin system API")
public record AdminSystemMetricResponse(
        @Schema(description = "Metric name", example = "jvm.memory.used")
        String name,
        @Schema(description = "Metric description", example = "The amount of used memory")
        String description,
        @Schema(description = "Metric base unit", example = "bytes")
        String baseUnit,
        @Schema(description = "Metric measurements")
        List<AdminSystemMetricMeasurementResponse> measurements,
        @Schema(description = "Available tag names and values")
        List<AdminSystemMetricTagResponse> availableTags
) {
}
