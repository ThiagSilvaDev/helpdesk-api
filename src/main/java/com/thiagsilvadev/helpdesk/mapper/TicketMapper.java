package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.TicketDto;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDto.TicketResponse toResponse(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketDto.TicketResponse(
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

    public Ticket toEntityFromUserRequest(TicketDto.UserCreateTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return toEntity(request.title(), request.description(), client, null);
    }

    public Ticket toEntityFromStaffRequest(TicketDto.StaffCreateTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return toEntity(request.title(), request.description(), client, request.priority());
    }

    private Ticket toEntity(String title, String description, User client, TicketPriority priority) {
        return new Ticket(title, description, client, priority);
    }

    private TicketDto.TicketResponse.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return new TicketDto.TicketResponse.UserInfo(
                user.getId(),
                user.getName()
        );
    }
}
