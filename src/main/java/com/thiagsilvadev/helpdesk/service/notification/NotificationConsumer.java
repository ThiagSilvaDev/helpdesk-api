package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.config.RabbitMqConfig;
import com.thiagsilvadev.helpdesk.dto.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.entity.Notification;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.ResourceNotFoundException;
import com.thiagsilvadev.helpdesk.exception.ResourceType;
import com.thiagsilvadev.helpdesk.repository.NotificationRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.notifications.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationConsumer(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATIONS_QUEUE)
    @Transactional
    public void handle(NotificationMessage message) {
        if (notificationRepository.existsBySourceEventId(message.sourceEventId())) {
            return;
        }

        User recipient = userRepository.findById(message.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.USER, message.recipientId()));
        notificationRepository.save(new Notification(
                recipient,
                message.type(),
                message.title(),
                message.message(),
                message.ticketId(),
                message.commentId(),
                message.actorUserId(),
                message.sourceEventId()
        ));
    }
}
