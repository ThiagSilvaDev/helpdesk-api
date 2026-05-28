package com.thiagsilvadev.helpdesk.api.ticket;

import com.thiagsilvadev.helpdesk.api.annotations.ApiByIdErrors;
import com.thiagsilvadev.helpdesk.api.annotations.ApiSecurityResponseErrors;
import com.thiagsilvadev.helpdesk.dto.ticket.AssignTechnicianRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketSearchCriteria;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketPriorityRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/staff/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Staff Tickets", description = "Ticket management for technicians and admins")
@SecurityRequirement(name = "bearerAuth")
@ApiSecurityResponseErrors
public interface StaffTicketApi {

    @PostMapping
    @Operation(operationId = "createTicketAsStaff")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created"),
        @ApiResponse(responseCode = "400", ref = "BadRequest"),
        @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketResponse> createTicketAsStaff(
            @RequestBody @Valid CreateStaffTicketRequest request, @CurrentUserId Long userId);

    @GetMapping("/{ticketId}")
    @Operation(operationId = "getTicketByIdForStaff")
    @ApiByIdErrors
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ticket found")})
    ResponseEntity<TicketResponse> getTicketByIdForStaff(@PathVariable Long ticketId);

    @GetMapping
    @Operation(operationId = "listTicketsForStaff")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tickets retrieved")})
    ResponseEntity<Page<TicketResponse>> listTicketsForStaff(
            @ParameterObject TicketSearchCriteria criteria, @ParameterObject Pageable pageable);

    @PatchMapping("/{ticketId}/priority")
    @Operation(operationId = "updateTicketPriorityAsStaff")
    @ApiByIdErrors
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Priority updated"),
        @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> updateTicketPriorityAsStaff(
            @PathVariable Long ticketId,
            @RequestBody @Valid UpdateTicketPriorityRequest request,
            @CurrentUserId Long userId);

    @PatchMapping("/{ticketId}/technician")
    @Operation(operationId = "assignTechnicianToTicket")
    @ApiByIdErrors
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Technician assigned"),
        @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> assignTechnicianToTicket(
            @PathVariable Long ticketId,
            @RequestBody @Valid AssignTechnicianRequest request,
            @CurrentUserId Long userId);

    @PatchMapping("/{ticketId}/close")
    @Operation(operationId = "closeTicketAsStaff")
    @ApiByIdErrors
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket closed"),
        @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<Void> closeTicketAsStaff(@PathVariable Long ticketId, @CurrentUserId Long userId);

    @PatchMapping("/{ticketId}/cancel")
    @Operation(operationId = "cancelTicketAsStaff")
    @ApiByIdErrors
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket cancelled"),
        @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<Void> cancelTicketAsStaff(@PathVariable Long ticketId, @CurrentUserId Long userId);
}
