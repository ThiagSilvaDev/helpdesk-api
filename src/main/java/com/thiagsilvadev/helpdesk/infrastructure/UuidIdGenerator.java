package com.thiagsilvadev.helpdesk.infrastructure;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidIdGenerator implements IdGenerator {

    @Override
    public UUID nextUuid() {
        return UUID.randomUUID();
    }
}
