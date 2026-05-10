package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketPriorityRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.ticket.Ticket;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.ticket.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.service.UserService;
import com.thiagsilvadev.helpdesk.service.notification.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
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
    private final NotificationDispatchService notificationDispatchService;
    private final Clock clock;

    public TicketCommandService(TicketRepository ticketRepository, UserService userService,
                                TicketQueryService ticketQueryService, TicketMapper ticketMapper,
                                NotificationDispatchService notificationDispatchService,
                                Clock clock) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.ticketQueryService = ticketQueryService;
        this.ticketMapper = ticketMapper;
        this.notificationDispatchService = notificationDispatchService;
        this.clock = clock;
    }


    @PreAuthorize("hasRole('USER')")
    @Transactional
    public TicketResponse createByUser(CreateUserTicketRequest userRequest, Long authenticatedUserId) {
        return createTicket(authenticatedUserId, authenticatedUserId, client -> ticketMapper.toEntityFromUserRequest(userRequest, client));
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Transactional
    public TicketResponse createByStaff(CreateStaffTicketRequest request, Long authenticatedUserId) {
        return createTicket(request.requesterId(), authenticatedUserId, client -> ticketMapper.toEntityFromStaffRequest(request, client));
    }

    private TicketResponse createTicket(Long clientId, Long actorUserId,
                                        Function<User, Ticket> ticketFactory) {
        User client = userService.getUserById(clientId);
        Ticket newTicket = ticketFactory.apply(client);
        Ticket savedTicket = ticketRepository.save(newTicket);
        notificationDispatchService.ticketCreated(savedTicket, actorUserId);

        log.atInfo()
                .setMessage("Ticket created successfully")
                .addKeyValue("ticketId", savedTicket.getId())
                .log();

        return ticketMapper.toResponse(savedTicket);
    }

    @PreAuthorize("@ticketAuthorization.canUpdate(#id, authentication)")
    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest request) {
        return modifyAndSave(id, ticket -> ticket.update(request.title(), request.description()));
    }

    @PreAuthorize("@ticketAuthorization.canUpdatePriority(authentication)")
    @Transactional
    public TicketResponse updatePriority(Long id, UpdateTicketPriorityRequest request, Long authenticatedUserId) {
        Ticket ticket = ticketQueryService.getTicketEntityById(id);
        TicketPriority previousPriority = ticket.getPriority();
        ticket.changePriority(request.priority());
        Ticket savedTicket = ticketRepository.save(ticket);
        notificationDispatchService.ticketPriorityChanged(savedTicket, previousPriority, authenticatedUserId);
        return ticketMapper.toResponse(savedTicket);
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Transactional
    public TicketResponse assignTechnician(Long ticketId, Long technicianId, Long authenticatedUserId) {
        Long userId = Objects.requireNonNullElse(technicianId, authenticatedUserId);
        User technician = userService.getUserById(userId);

        log.info("Assigning technician {} to ticket {}", technicianId, ticketId);

        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        TicketStatus previousStatus = ticket.getStatus();
        ticket.assignTechnician(technician);
        Ticket savedTicket = ticketRepository.save(ticket);
        notificationDispatchService.ticketAssigned(savedTicket, authenticatedUserId);
        notificationDispatchService.ticketStatusChanged(savedTicket, previousStatus, authenticatedUserId);
        return ticketMapper.toResponse(savedTicket);
    }

    @PreAuthorize("@ticketAuthorization.canClose(#id, authentication)")
    @Transactional
    public void close(Long id, Long authenticatedUserId) {
        log.info("Closing ticket with id {}", id);
        changeStatus(id, authenticatedUserId, ticket -> ticket.closeTicket(clock.instant()));
    }

    @PreAuthorize("@ticketAuthorization.canCancel(#id, authentication)")
    @Transactional
    public void cancel(Long id, Long authenticatedUserId) {
        log.info("Canceling ticket with id {}", id);
        changeStatus(id, authenticatedUserId, Ticket::cancelTicket);
    }

    private TicketResponse modifyAndSave(Long ticketId, Consumer<Ticket> action) {
        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        action.accept(ticket);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private void changeStatus(Long ticketId, Long actorUserId, Consumer<Ticket> action) {
        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        TicketStatus previousStatus = ticket.getStatus();
        action.accept(ticket);
        Ticket savedTicket = ticketRepository.save(ticket);
        notificationDispatchService.ticketStatusChanged(savedTicket, previousStatus, actorUserId);
    }
}
