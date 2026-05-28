package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.NotificationApi;
import com.thiagsilvadev.helpdesk.dto.notification.NotificationResponse;
import com.thiagsilvadev.helpdesk.dto.notification.UnreadNotificationCountResponse;
import com.thiagsilvadev.helpdesk.security.web.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public ResponseEntity<Page<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly, @CurrentUserId Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.findForUser(userId, unreadOnly, pageable));
    }

    @Override
    public ResponseEntity<UnreadNotificationCountResponse> countUnreadNotifications(@CurrentUserId Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @Override
    public ResponseEntity<NotificationResponse> markNotificationRead(
            @PathVariable Long id, @CurrentUserId Long userId) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    @Override
    public ResponseEntity<Void> markAllNotificationsRead(@CurrentUserId Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
