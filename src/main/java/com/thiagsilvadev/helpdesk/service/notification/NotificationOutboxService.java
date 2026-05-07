package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.dto.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.entity.NotificationOutboxEvent;
import com.thiagsilvadev.helpdesk.repository.NotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;

    public NotificationOutboxService(NotificationOutboxRepository notificationOutboxRepository,
                                     ObjectMapper objectMapper) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enqueue(NotificationMessage message) {
        try {
            notificationOutboxRepository.save(new NotificationOutboxEvent(
                    message.sourceEventId(),
                    objectMapper.writeValueAsString(message)
            ));
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize notification message", ex);
        }
    }
}
