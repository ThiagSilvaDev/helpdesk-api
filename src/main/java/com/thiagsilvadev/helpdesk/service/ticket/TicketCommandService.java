package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.ticket.*;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class TicketCommandService {

    private static final Logger log = LoggerFactory.getLogger(TicketCommandService.class);

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final TicketQueryService ticketQueryService;
    private final TicketMapper ticketMapper;

    public TicketCommandService(TicketRepository ticketRepository, UserService userService,
                                TicketQueryService ticketQueryService, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.ticketQueryService = ticketQueryService;
        this.ticketMapper = ticketMapper;
    }


    @PreAuthorize("hasRole('USER')")
    @Transactional
    public TicketResponse createByUser(UserCreateTicketRequest request, Long authenticatedUserId) {
        return createTicket(authenticatedUserId, client -> ticketMapper.toEntity(request, client));
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Transactional
    public TicketResponse createByStaff(StaffCreateTicketRequest request) {
        return createTicket(request.requesterId(), client -> ticketMapper.toEntity(request, client));
    }

    private TicketResponse createTicket(Long clientId, Function<User, Ticket> ticketFactory) {
        User client = userService.getUserById(clientId);
        Ticket newTicket = ticketFactory.apply(client);
        Ticket savedTicket = ticketRepository.save(newTicket);

        log.info("Ticket created successfully with id {}", savedTicket.getId());

        return ticketMapper.toResponse(savedTicket);
    }

    @PreAuthorize("@ticketAuthorization.canUpdate(#id, authentication)")
    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest request) {
        return modifyAndSave(id, ticket -> ticket.update(request.title(), request.description()));
    }

    @PreAuthorize("@ticketAuthorization.canUpdatePriority(authentication)")
    @Transactional
    public TicketResponse updatePriority(Long id, UpdatePriorityRequest request) {
        return modifyAndSave(id, ticket -> ticket.changePriority(request.priority()));
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Transactional
    public TicketResponse assignTechnician(Long ticketId, Long technicianId, Long authenticatedUserId) {
        Long userId = Objects.requireNonNullElse(technicianId, authenticatedUserId);
        User technician = userService.getUserById(userId);

        log.info("Assigning technician {} to ticket {}", technicianId, ticketId);

        return modifyAndSave(ticketId, ticket -> ticket.assignTechnician(technician));
    }

    @PreAuthorize("@ticketAuthorization.canClose(#id, authentication)")
    @Transactional
    public void close(Long id) {
        log.info("Closing ticket with id {}", id);
        modifyAndSaveVoid(id, Ticket::closeTicket);
    }

    @PreAuthorize("@ticketAuthorization.canCancel(#id, authentication)")
    @Transactional
    public void cancel(Long id) {
        log.info("Canceling ticket with id {}", id);
        modifyAndSaveVoid(id, Ticket::cancelTicket);
    }

    private TicketResponse modifyAndSave(Long ticketId, Consumer<Ticket> action) {
        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        action.accept(ticket);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private void modifyAndSaveVoid(Long ticketId, Consumer<Ticket> action) {
        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        action.accept(ticket);
        ticketRepository.save(ticket);
    }
}
