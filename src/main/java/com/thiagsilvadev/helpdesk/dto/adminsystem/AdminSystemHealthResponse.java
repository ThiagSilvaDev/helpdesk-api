package com.thiagsilvadev.helpdesk.dto.adminsystem;

import java.util.List;

public record AdminSystemHealthResponse(
        String status,
        List<AdminSystemHealthComponentResponse> components
) {
}
