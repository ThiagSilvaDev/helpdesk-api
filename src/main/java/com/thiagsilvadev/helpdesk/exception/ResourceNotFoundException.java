package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ResourceNotFoundException extends BusinessRuleException {
    
    public ResourceNotFoundException(ResourceType resourceType, Object identifier) {
        super(HttpStatus.NOT_FOUND, "Resource not found.", Map.of(
                "resourceName", resourceType.label(),
                "identifier", identifier
        ));
    }
}
