package com.thiagsilvadev.helpdesk.api.ticket;

import com.thiagsilvadev.helpdesk.api.annotations.ApiByIdErrors;
import com.thiagsilvadev.helpdesk.api.annotations.ApiSecurityResponseErrors;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.security.web.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
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
@ApiSecurityResponseErrors
public interface UserTicketApi {

    @PostMapping
    @Operation(operationId = "createAuthenticatedUserTicket")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket created"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<TicketResponse> createTicketAsUser(
            @RequestBody @Valid CreateUserTicketRequest userRequest,
            @CurrentUserId Long userId
    );

    @GetMapping("/{ticketId}")
    @Operation(operationId = "getAuthenticatedUserTicketById")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket found"
            )
    })
    ResponseEntity<TicketResponse> getTicketByIdForAuthenticatedUser(
            @PathVariable Long ticketId,
            @CurrentUserId Long userId
    );

    @GetMapping
    @Operation(operationId = "listAuthenticatedUserTickets")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Own tickets retrieved"
            )
    })
    ResponseEntity<Page<TicketResponse>> listAuthenticatedUserTickets(
            @CurrentUserId Long userId,
            @ParameterObject Pageable pageable
    );

    @PutMapping("/{id}")
    @Operation(operationId = "updateAuthenticatedUserTicket")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket updated"
            ),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> updateTicketAsUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTicketRequest request
    );
}
