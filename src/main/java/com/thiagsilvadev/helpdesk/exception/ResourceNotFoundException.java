package com.thiagsilvadev.helpdesk.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessRuleException {

    public ResourceNotFoundException(ResourceType resourceType, Object identifier) {
        super(
                HttpStatus.NOT_FOUND,
                "Resource not found.",
                Map.of("resourceName", resourceType.label(), "identifier", identifier));
    }
}
