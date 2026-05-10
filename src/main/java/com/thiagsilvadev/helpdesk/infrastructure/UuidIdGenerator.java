package com.thiagsilvadev.helpdesk.infrastructure;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidIdGenerator implements IdGenerator {

    @Override
    public UUID nextUuid() {
        return UUID.randomUUID();
    }
}
