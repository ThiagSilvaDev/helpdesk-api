package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/users/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Tickets", description = "Ticket operations for authenticated users (own tickets)")
@SecurityRequirement(name = "bearerAuth")
public interface UserTicketApi {

    @PostMapping
    @Operation(operationId = "createAuthenticatedUserTicket", summary = "Create ticket", description = "Creates a new ticket as the authenticated user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket created"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<TicketResponse> createTicketAsUser(
            @RequestBody @Valid CreateUserTicketRequest userRequest,
            @CurrentUserId Long userId
    );

    @GetMapping("/{ticketId}")
    @Operation(operationId = "getAuthenticatedUserTicketById", summary = "Get own ticket", description = "Returns a ticket owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket found"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketResponse> getTicketByIdForAuthenticatedUser(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @CurrentUserId Long userId
    );

    @GetMapping
    @Operation(operationId = "listAuthenticatedUserTickets", summary = "List own tickets", description = "Returns a paginated list of the authenticated user's tickets")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Own tickets retrieved"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    ResponseEntity<Page<TicketResponse>> listAuthenticatedUserTickets(
            @CurrentUserId Long userId,
            @ParameterObject Pageable pageable
    );

    @PutMapping("/{id}")
    @Operation(operationId = "updateAuthenticatedUserTicket", summary = "Update ticket", description = "Updates title and description of an existing ticket")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket updated"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> updateTicketAsUser(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long id,
            @RequestBody @Valid UpdateTicketRequest request
    );
}
