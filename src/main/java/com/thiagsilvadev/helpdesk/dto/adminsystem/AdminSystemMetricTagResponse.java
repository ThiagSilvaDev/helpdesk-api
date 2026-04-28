package com.thiagsilvadev.helpdesk.dto.adminsystem;

import java.util.List;

public record AdminSystemMetricTagResponse(
        String tag,
        List<String> values
) {
}
