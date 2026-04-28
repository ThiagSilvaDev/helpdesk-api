package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentAuthorInfo;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentResponse;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketComment;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketCommentMapper {

    public TicketCommentResponse toResponse(TicketComment comment) {
        if (comment == null) {
            return null;
        }

        return new TicketCommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                toAuthorInfo(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public TicketComment toEntity(Ticket ticket, User author, String content) {
        if (ticket == null || author == null) {
            return null;
        }

        return new TicketComment(ticket, author, content);
    }

    private TicketCommentAuthorInfo toAuthorInfo(User author) {
        if (author == null) {
            return null;
        }

        return new TicketCommentAuthorInfo(
                author.getId(),
                author.getName()
        );
    }
}
