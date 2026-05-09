package com.thiagsilvadev.helpdesk.service.notification;

import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;
import com.thiagsilvadev.helpdesk.entity.ticket.Ticket;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketComment;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.messaging.notification.NotificationMessage;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class NotificationDispatchService {

    private final UserRepository userRepository;
    private final NotificationOutboxService notificationOutboxService;

    public NotificationDispatchService(UserRepository userRepository,
                                       NotificationOutboxService notificationOutboxService) {
        this.userRepository = userRepository;
        this.notificationOutboxService = notificationOutboxService;
    }

    public void ticketCreated(Ticket ticket, Long actorUserId) {
        Set<Long> recipients = staffPool();
        recipients.add(ticket.getClient().getId());
        publish(recipients, actorUserId, NotificationType.TICKET_CREATED, "Ticket created",
                "Ticket #%d was created: %s".formatted(ticket.getId(), ticket.getTitle()),
                ticket.getId(), null);
    }

    public void ticketAssigned(Ticket ticket, Long actorUserId) {
        Set<Long> recipients = participants(ticket);
        publish(recipients, actorUserId, NotificationType.TICKET_ASSIGNED, "Ticket assigned",
                "Ticket #%d was assigned to %s".formatted(ticket.getId(), ticket.getTechnician().getName()),
                ticket.getId(), null);
    }

    public void ticketPriorityChanged(Ticket ticket, TicketPriority previousPriority, Long actorUserId) {
        if (previousPriority == ticket.getPriority()) {
            return;
        }
        publish(participants(ticket), actorUserId, NotificationType.TICKET_PRIORITY_CHANGED, "Ticket priority changed",
                "Ticket #%d priority changed from %s to %s".formatted(ticket.getId(), previousPriority, ticket.getPriority()),
                ticket.getId(), null);
    }

    public void ticketStatusChanged(Ticket ticket, TicketStatus previousStatus, Long actorUserId) {
        if (previousStatus == ticket.getStatus()) {
            return;
        }
        publish(participants(ticket), actorUserId, NotificationType.TICKET_STATUS_CHANGED, "Ticket status changed",
                "Ticket #%d status changed from %s to %s".formatted(ticket.getId(), previousStatus, ticket.getStatus()),
                ticket.getId(), null);
    }

    public void ticketCommentCreated(TicketComment comment, Long actorUserId) {
        Ticket ticket = comment.getTicket();
        Set<Long> recipients = participants(ticket);
        if (ticket.getTechnician() == null && actorUserId != null && actorUserId.equals(ticket.getClient().getId())) {
            recipients.addAll(staffPool());
        }
        publish(recipients, actorUserId, NotificationType.TICKET_COMMENT_CREATED, "New ticket comment",
                "A new comment was added to ticket #%d".formatted(ticket.getId()), ticket.getId(), comment.getId());
    }

    private Set<Long> participants(Ticket ticket) {
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(ticket.getClient().getId());
        if (ticket.getTechnician() != null) {
            recipients.add(ticket.getTechnician().getId());
        }
        return recipients;
    }

    private Set<Long> staffPool() {
        Set<Long> recipients = new LinkedHashSet<>();
        userRepository.findByRoleInAndActiveTrue(List.of(Roles.ROLE_ADMIN, Roles.ROLE_TECHNICIAN))
                .forEach(user -> recipients.add(user.getId()));
        return recipients;
    }

    private void publish(Set<Long> recipients, Long actorUserId, NotificationType type, String title,
                         String message, Long ticketId, Long commentId) {
        recipients.stream()
                .filter(recipientId -> actorUserId == null || !actorUserId.equals(recipientId))
                .forEach(recipientId -> notificationOutboxService.enqueue(new NotificationMessage(
                        UUID.randomUUID(),
                        recipientId,
                        type,
                        title,
                        message,
                        ticketId,
                        commentId,
                        actorUserId
                )));
    }
}
