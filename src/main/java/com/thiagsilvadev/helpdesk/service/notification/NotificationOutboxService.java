package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxEvent;
import com.thiagsilvadev.helpdesk.messaging.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.repository.NotificationOutboxRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public NotificationOutboxService(
            NotificationOutboxRepository notificationOutboxRepository, ObjectMapper objectMapper, Clock clock) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public void enqueue(NotificationMessage message) {
        try {
            notificationOutboxRepository.save(new NotificationOutboxEvent(
                    message.sourceEventId(), objectMapper.writeValueAsString(message), clock.instant()));
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize notification message", ex);
        }
    }
}
