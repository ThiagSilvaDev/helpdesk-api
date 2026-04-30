package com.thiagsilvadev.helpdesk.exception;

import org.springframework.web.client.HttpClientErrorException;

public enum ResourceType {
    USER("User"),
    TICKET("Ticket"),
    COMMENT("Comment"),
    METRIC("Metric");

    private final String label;

    ResourceType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
