package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.config.messaging.RabbitMqConfig;
import com.thiagsilvadev.helpdesk.dto.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxEvent;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxStatus;
import com.thiagsilvadev.helpdesk.repository.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.notifications.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxPublisher.class);

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public NotificationOutboxPublisher(NotificationOutboxRepository notificationOutboxRepository,
                                       RabbitTemplate rabbitTemplate,
                                       ObjectMapper objectMapper) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.notifications.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<NotificationOutboxEvent> events = notificationOutboxRepository.findPublishable(
                List.of(NotificationOutboxStatus.PENDING, NotificationOutboxStatus.FAILED),
                Instant.now(),
                PageRequest.of(0, 50)
        );

        for (NotificationOutboxEvent event : events) {
            try {
                event.retry();
                NotificationMessage message = objectMapper.readValue(event.getPayload(), NotificationMessage.class);
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.NOTIFICATIONS_EXCHANGE,
                        RabbitMqConfig.NOTIFICATIONS_ROUTING_KEY,
                        message
                );
                event.markPublished();
            } catch (Exception ex) {
                event.markFailed(ex.getMessage());
                log.warn("Failed to publish notification outbox event {}", event.getId(), ex);
            }
        }
    }
}
