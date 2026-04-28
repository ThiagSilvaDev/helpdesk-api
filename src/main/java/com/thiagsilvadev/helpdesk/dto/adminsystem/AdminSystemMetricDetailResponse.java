package com.thiagsilvadev.helpdesk.dto.adminsystem;

import java.util.List;

public record AdminSystemMetricDetailResponse(
        String name,
        String description,
        String baseUnit,
        List<AdminSystemMetricMeasurementResponse> measurements,
        List<AdminSystemMetricTagResponse> availableTags
) {
}
