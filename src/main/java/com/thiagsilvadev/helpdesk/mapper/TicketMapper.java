package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.ticket.StaffCreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UserCreateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                toUserInfo(ticket.getClient()),
                toUserInfo(ticket.getTechnician()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }

    public Ticket toEntity(UserCreateTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return new Ticket(request.title(), request.description(), client, null);
    }

    public Ticket toEntity(StaffCreateTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return new Ticket(request.title(), request.description(), client, request.priority());
    }

    private TicketResponse.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return new TicketResponse.UserInfo(
                user.getId(),
                user.getName()
        );
    }
}
