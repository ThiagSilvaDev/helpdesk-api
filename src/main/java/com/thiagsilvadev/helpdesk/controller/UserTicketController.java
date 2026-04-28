package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public ResponseEntity<TicketResponse> createTicketAsUser(@RequestBody @Valid CreateUserTicketRequest userRequest,
                                                                 @Parameter(hidden = true) @CurrentUserId Long userId) {
        TicketResponse newTicket = ticketCommandService.createByUser(userRequest, userId);
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
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketResponse> getTicketByIdForAuthenticatedUser(@PathVariable Long ticketId,
                                                                                 @Parameter(hidden = true) @CurrentUserId Long userId) {
        TicketResponse ticket = ticketQueryService.getOwnTicketById(ticketId, userId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List own tickets", description = "Returns a paginated list of the authenticated user's tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Own tickets retrieved")})
    public ResponseEntity<Page<TicketResponse>> listAuthenticatedUserTickets(@Parameter(hidden = true) @CurrentUserId Long userId,
                                                                                 @ParameterObject Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findTicketsByClientId(userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Updates title and description of an existing ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketResponse> updateTicketAsUser(@PathVariable Long id, @RequestBody @Valid UpdateTicketRequest request) {
        TicketResponse updatedTicket = ticketCommandService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }
}
