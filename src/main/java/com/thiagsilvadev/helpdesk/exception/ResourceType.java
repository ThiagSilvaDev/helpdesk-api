package com.thiagsilvadev.helpdesk.exception;

public enum ResourceType {
    USER("User"),
    TICKET("Ticket"),
    COMMENT("Comment"),
    NOTIFICATION("Notification"),
    METRIC("Metric");

    private final String label;

    ResourceType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
