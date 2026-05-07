package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.dto.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.entity.notification.Notification;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.repository.NotificationRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void shouldCreateNotificationFromMessage() {
        UUID sourceEventId = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(
                sourceEventId,
                10L,
                NotificationType.TICKET_ASSIGNED,
                "Ticket assigned",
                "Ticket #100 was assigned",
                100L,
                null,
                20L
        );
        User recipient = new User("Jane User", "jane@helpdesk.local", "secret", Roles.ROLE_USER);

        given(notificationRepository.existsBySourceEventId(sourceEventId)).willReturn(false);
        given(userRepository.findById(10L)).willReturn(Optional.of(recipient));

        notificationConsumer.handle(message);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());
        Notification notification = captor.getValue();
        assertThat(notification.getRecipient()).isSameAs(recipient);
        assertThat(notification.getType()).isEqualTo(NotificationType.TICKET_ASSIGNED);
        assertThat(notification.getTicketId()).isEqualTo(100L);
        assertThat(notification.getSourceEventId()).isEqualTo(sourceEventId);
    }

    @Test
    void shouldIgnoreDuplicateSourceEvent() {
        UUID sourceEventId = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(
                sourceEventId,
                10L,
                NotificationType.TICKET_ASSIGNED,
                "Ticket assigned",
                "Ticket #100 was assigned",
                100L,
                null,
                20L
        );

        given(notificationRepository.existsBySourceEventId(sourceEventId)).willReturn(true);

        notificationConsumer.handle(message);

        then(userRepository).should(never()).findById(10L);
        then(notificationRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }
}
