package com.thiagsilvadev.helpdesk.infrastructure;

import java.util.UUID;

public interface IdGenerator {

    UUID nextUuid();

    default String nextUuidString() {
        return nextUuid().toString();
    }
}
