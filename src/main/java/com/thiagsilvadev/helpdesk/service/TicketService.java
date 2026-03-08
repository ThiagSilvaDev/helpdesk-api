package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;

    public TicketService(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    public Ticket create(String title, String description, Long clientId) {
        User client = userService.getUserById(clientId);

        Ticket newTicket = new Ticket(title, description, client);
        return ticketRepository.save(newTicket);
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Ticket update(Long id, Ticket ticket) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setTitle(ticket.getTitle());
        existingTicket.setDescription(ticket.getDescription());

        return ticketRepository.save(existingTicket);
    }

    public Ticket assignTechnician(Long ticketId, Long technicianId) {
        Ticket existingTicket = getTicketById(ticketId);
        User technician = userService.getUserById(technicianId);
        existingTicket.setTechnician(technician);
        existingTicket.inProgressTicket();

        return ticketRepository.save(existingTicket);
    }

    public void close(Long id) {
        Ticket existingTicket = getTicketById(id);

        if (existingTicket.getStatus() == TicketStatus.CLOSED) {
            throw new RuntimeException("Ticket is already closed with id: " + id);
        }
        existingTicket.closeTicket();

        ticketRepository.save(existingTicket);
    }

    public void cancel(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.cancelTicket();
        ticketRepository.save(existingTicket);
    }
}
