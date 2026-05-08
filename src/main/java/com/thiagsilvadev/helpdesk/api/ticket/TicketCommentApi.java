package com.thiagsilvadev.helpdesk.api.ticket;

import com.thiagsilvadev.helpdesk.api.ApiByIdErrors;
import com.thiagsilvadev.helpdesk.api.ApiSecurityResponseErrors;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.CreateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentResponse;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.UpdateTicketCommentRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/tickets/{ticketId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Ticket Comments", description = "Comments attached to tickets")
@SecurityRequirement(name = "bearerAuth")
@ApiSecurityResponseErrors
public interface TicketCommentApi {

    @PostMapping
    @Operation(operationId = "createTicketComment")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created"
            )
    })
    ResponseEntity<TicketCommentResponse> createTicketComment(
            @PathVariable Long ticketId,
            @RequestBody @Valid CreateTicketCommentRequest request,
            @CurrentUserId Long userId
    );

    @GetMapping
    @Operation(operationId = "listTicketComments")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved"
            )
    })
    ResponseEntity<Page<TicketCommentResponse>> listTicketComments(
            @PathVariable Long ticketId,
            @ParameterObject Pageable pageable
    );

    @PutMapping("/{commentId}")
    @Operation(operationId = "updateTicketComment")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated"
            )
    })
    ResponseEntity<TicketCommentResponse> updateTicketComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateTicketCommentRequest request
    );

    @DeleteMapping("/{commentId}")
    @Operation(operationId = "deleteTicketComment")
    @ApiByIdErrors
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted")
    })
    ResponseEntity<Void> deleteTicketComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId
    );
}
