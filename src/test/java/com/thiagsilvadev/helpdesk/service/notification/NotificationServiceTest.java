package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.entity.notification.Notification;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.mapper.NotificationMapper;
import com.thiagsilvadev.helpdesk.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-09T18:00:00Z");

    @Mock
    private NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper();

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                notificationMapper,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldMarkCurrentUsersNotificationAsRead() {
        Notification notification = new Notification(
                new User("Jane User", "jane@helpdesk.local", "secret", Roles.ROLE_USER),
                NotificationType.TICKET_STATUS_CHANGED,
                "Ticket status changed",
                "Ticket #100 status changed",
                100L,
                null,
                20L,
                UUID.randomUUID()
        );

        given(notificationRepository.findByIdAndRecipientId(1L, 10L)).willReturn(Optional.of(notification));
        given(notificationRepository.save(notification)).willReturn(notification);

        var response = notificationService.markAsRead(1L, 10L);

        assertThat(response.read()).isTrue();
        assertThat(response.readAt()).isEqualTo(NOW);
    }
}
