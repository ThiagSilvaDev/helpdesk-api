package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.*;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users/tickets")
@Tag(name = "User Tickets", description = "Ticket operations for authenticated users (own tickets)")
@SecurityRequirement(name = "bearerAuth")
public class UserTicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public UserTicketController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @PostMapping
    @Operation(summary = "Create ticket", description = "Creates a new ticket as the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied — only ROLE_USER can create tickets")
    })
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid UserCreateTicketRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        TicketResponse newTicket = ticketCommandService.createByUser(request, principal.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTicket.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(newTicket);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get own ticket", description = "Returns a ticket owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found or not owned by user")
    })
    public ResponseEntity<TicketResponse> getOwnTicketById(@PathVariable Long ticketId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        TicketResponse ticket = ticketQueryService.getOwnTicketById(ticketId, principal.getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    @Operation(summary = "List own tickets", description = "Returns a paginated list of the authenticated user's tickets")
    @ApiResponse(responseCode = "200", description = "Own tickets retrieved")
    public ResponseEntity<Page<TicketResponse>> getUserTickets(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findTicketsByClientId(principal.getId(), pageable);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Updates title and description of an existing ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "422", description = "Invalid ticket state for update")
    })
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateTicketRequest request) {
        TicketResponse updatedTicket = ticketCommandService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }
}
