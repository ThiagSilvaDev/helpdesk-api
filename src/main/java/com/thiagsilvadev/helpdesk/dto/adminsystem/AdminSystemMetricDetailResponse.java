package com.thiagsilvadev.helpdesk.dto.adminsystem;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminSystemMetricDetailResponse(
        String name,
        String description,
        String baseUnit,
        @ArraySchema(schema = @Schema(implementation = AdminSystemMetricMeasurementResponse.class))
        List<AdminSystemMetricMeasurementResponse> measurements,
        @ArraySchema(schema = @Schema(implementation = AdminSystemMetricTagResponse.class))
        List<AdminSystemMetricTagResponse> availableTags
) {
}
