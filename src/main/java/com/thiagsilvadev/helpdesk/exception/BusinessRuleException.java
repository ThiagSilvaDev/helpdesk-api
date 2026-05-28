package com.thiagsilvadev.helpdesk.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public abstract class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> property;

    protected BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.property = null;
    }

    protected BusinessRuleException(HttpStatus status, String detail, Map<String, Object> property) {
        super(detail);
        this.status = status;
        this.property = property;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getProperty() {
        return property;
    }
}
