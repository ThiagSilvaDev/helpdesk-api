package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.*;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/tickets")
public class UserTicketController {

    private final TicketCommandService ticketCommandServiceService;
    private final TicketQueryService ticketQueryService;

    public UserTicketController(TicketCommandService ticketCommandServiceService, TicketQueryService ticketQueryService) {
        this.ticketCommandServiceService = ticketCommandServiceService;
        this.ticketQueryService = ticketQueryService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid UserCreateTicketRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        TicketResponse newTicket = ticketCommandServiceService.createByUser(request, principal.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTicket.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(newTicket);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getOwnTicketById(@PathVariable Long ticketId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        TicketResponse ticket = ticketQueryService.getOwnTicketById(ticketId, principal.getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getUserTickets(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findTicketsByClientId(principal.getId(), pageable);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateTicketRequest request) {
        TicketResponse updatedTicket = ticketCommandServiceService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }
}
