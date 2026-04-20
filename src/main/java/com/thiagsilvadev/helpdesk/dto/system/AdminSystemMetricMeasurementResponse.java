package com.thiagsilvadev.helpdesk.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single measurement sample for a metric")
public record AdminSystemMetricMeasurementResponse(
        @Schema(description = "Micrometer statistic name", example = "COUNT")
        String statistic,
        @Schema(description = "Metric value", example = "42.0")
        Double value
) {
}
