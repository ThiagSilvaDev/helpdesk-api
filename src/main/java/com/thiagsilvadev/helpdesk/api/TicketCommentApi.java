package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.ticketcomment.CreateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentResponse;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.UpdateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@RequestMapping("/api/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments", description = "Comments attached to tickets")
@SecurityRequirement(name = "bearerAuth")
public interface TicketCommentApi {

    @PostMapping
    @Operation(operationId = "createTicketComment", summary = "Create ticket comment", description = "Adds a comment to a ticket")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TicketCommentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketCommentResponse> createTicketComment(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @RequestBody @Valid CreateTicketCommentRequest request,
            @Parameter(hidden = true) @CurrentUserId Long userId
    );

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "listTicketComments", summary = "List ticket comments", description = "Returns a paginated list of comments for a ticket")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<Page<TicketCommentResponse>> listTicketComments(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @ParameterObject Pageable pageable
    );

    @PutMapping("/{commentId}")
    @Operation(operationId = "updateTicketComment", summary = "Update ticket comment", description = "Updates an existing ticket comment")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TicketCommentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<TicketCommentResponse> updateTicketComment(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @Parameter(description = "Comment id", example = "200")
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateTicketCommentRequest request
    );

    @DeleteMapping("/{commentId}")
    @Operation(operationId = "deleteTicketComment", summary = "Delete ticket comment", description = "Deletes an existing ticket comment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<Void> deleteTicketComment(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @Parameter(description = "Comment id", example = "200")
            @PathVariable Long commentId
    );
}
