package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.ticket.UserCreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.StaffCreateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketRequestMapper {

    public Ticket toEntity(UserCreateTicketRequest request, User client) {
        if (request == null || client == null) return null;

        return new Ticket(request.title(), request.description(), client, null);
    }

    public Ticket toEntity(StaffCreateTicketRequest request, User client) {
        if (request == null || client == null) return null;

        return new Ticket(request.title(), request.description(), client, request.priority());
    }
}
