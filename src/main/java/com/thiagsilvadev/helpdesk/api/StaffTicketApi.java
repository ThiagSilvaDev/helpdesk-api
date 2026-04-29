package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.ticket.AssignTechnicianRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketSearchCriteria;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketPriorityRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/staff/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Staff Tickets", description = "Ticket management for technicians and admins")
@SecurityRequirement(name = "bearerAuth")
public interface StaffTicketApi {

    @PostMapping
    @Operation(operationId = "createTicketAsStaff", summary = "Create ticket for requester", description = "Staff creates a ticket on behalf of a user, with explicit priority")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket created"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketResponse> createTicketAsStaff(@RequestBody @Valid CreateStaffTicketRequest request);

    @GetMapping("/{ticketId}")
    @Operation(operationId = "getTicketByIdForStaff", summary = "Get ticket by ID", description = "Returns a single ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket found"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketResponse> getTicketByIdForStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    );

    @GetMapping
    @Operation(operationId = "listTicketsForStaff", summary = "List all tickets", description = "Returns a paginated, filterable list of all tickets (admin/technician)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tickets retrieved"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<Page<TicketResponse>> listTicketsForStaff(
            @ParameterObject TicketSearchCriteria criteria,
            @ParameterObject Pageable pageable
    );

    @PatchMapping("/{ticketId}/priority")
    @Operation(operationId = "updateTicketPriorityAsStaff", summary = "Update ticket priority", description = "Changes the priority of a ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Priority updated"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> updateTicketPriorityAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @RequestBody @Valid UpdateTicketPriorityRequest request
    );

    @PatchMapping("/{ticketId}/technician")
    @Operation(operationId = "assignTechnicianToTicket", summary = "Assign technician", description = "Assigns a technician to a ticket and sets status to IN_PROGRESS")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Technician assigned"
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<TicketResponse> assignTechnicianToTicket(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @RequestBody @Valid AssignTechnicianRequest request,
            @CurrentUserId Long userId
    );

    @PatchMapping("/{ticketId}/close")
    @Operation(operationId = "closeTicketAsStaff", summary = "Close ticket", description = "Closes a ticket (admin, or assigned technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket closed"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<Void> closeTicketAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    );

    @PatchMapping("/{ticketId}/cancel")
    @Operation(operationId = "cancelTicketAsStaff", summary = "Cancel ticket", description = "Cancels a ticket (admin, technician, or ticket owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket cancelled"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    ResponseEntity<Void> cancelTicketAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    );
}
