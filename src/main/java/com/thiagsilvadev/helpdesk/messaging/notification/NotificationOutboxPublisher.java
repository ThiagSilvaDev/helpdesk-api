package com.thiagsilvadev.helpdesk.messaging.notification;

import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxEvent;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxStatus;
import com.thiagsilvadev.helpdesk.messaging.rabbitmq.RabbitMqConfig;
import com.thiagsilvadev.helpdesk.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(
        prefix = "app.notifications.rabbit",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class NotificationOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxPublisher.class);

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public NotificationOutboxPublisher(
            NotificationOutboxRepository notificationOutboxRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            Clock clock) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.notifications.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<NotificationOutboxEvent> events = notificationOutboxRepository.findPublishable(
                List.of(NotificationOutboxStatus.PENDING, NotificationOutboxStatus.FAILED),
                Instant.from(clock.instant()),
                PageRequest.of(0, 50));

        for (NotificationOutboxEvent event : events) {
            try {
                event.retry();
                NotificationMessage message = objectMapper.readValue(event.getPayload(), NotificationMessage.class);
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.NOTIFICATIONS_EXCHANGE, RabbitMqConfig.NOTIFICATIONS_ROUTING_KEY, message);
                event.markPublished(clock.instant());
            } catch (Exception ex) {
                event.markFailed(ex.getMessage(), clock.instant());
                log.warn("Failed to publish notification outbox event {}", event.getId(), ex);
            }
        }
    }
}
