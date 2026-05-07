package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.dto.notification.NotificationResponse;
import com.thiagsilvadev.helpdesk.dto.notification.UnreadNotificationCountResponse;
import com.thiagsilvadev.helpdesk.entity.Notification;
import com.thiagsilvadev.helpdesk.exception.ResourceNotFoundException;
import com.thiagsilvadev.helpdesk.exception.ResourceType;
import com.thiagsilvadev.helpdesk.mapper.NotificationMapper;
import com.thiagsilvadev.helpdesk.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @PreAuthorize("isAuthenticated()")
    public Page<NotificationResponse> findForUser(Long userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> notifications = unreadOnly
                ? notificationRepository.findByRecipientIdAndReadAtIsNull(userId, pageable)
                : notificationRepository.findByRecipientId(userId, pageable);
        return notifications.map(notificationMapper::toResponse);
    }

    @PreAuthorize("isAuthenticated()")
    public UnreadNotificationCountResponse countUnread(Long userId) {
        return new UnreadNotificationCountResponse(notificationRepository.countByRecipientIdAndReadAtIsNull(userId));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.NOTIFICATION, notificationId));
        notification.markAsRead();
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, Instant.now());
    }
}
