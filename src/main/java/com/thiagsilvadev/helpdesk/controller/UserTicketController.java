package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketDto;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springdoc.core.annotations.ParameterObject;
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
            @ApiResponse(responseCode = "201", description = "Ticket created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public ResponseEntity<TicketDto.TicketResponse> createTicketAsUser(@RequestBody @Valid TicketDto.UserCreateTicketRequest request,
                                                                       @AuthenticationPrincipal UserPrincipal principal) {
        TicketDto.TicketResponse newTicket = ticketCommandService.createByUser(request, principal.getId());
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
            @ApiResponse(responseCode = "200", description = "Ticket found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketDto.TicketResponse> getTicketByIdForAuthenticatedUser(@PathVariable Long ticketId,
                                                                                       @AuthenticationPrincipal UserPrincipal principal) {
        TicketDto.TicketResponse ticket = ticketQueryService.getOwnTicketById(ticketId, principal.getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List own tickets", description = "Returns a paginated list of the authenticated user's tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Own tickets retrieved")})
    public ResponseEntity<Page<TicketDto.TicketResponse>> listAuthenticatedUserTickets(@AuthenticationPrincipal UserPrincipal principal,
                                                                                        @ParameterObject Pageable pageable) {
        Page<TicketDto.TicketResponse> tickets = ticketQueryService.findTicketsByClientId(principal.getId(), pageable);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Updates title and description of an existing ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketDto.TicketResponse> updateTicketAsUser(@PathVariable Long id, @RequestBody @Valid TicketDto.UpdateTicketRequest request) {
        TicketDto.TicketResponse updatedTicket = ticketCommandService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }
}
