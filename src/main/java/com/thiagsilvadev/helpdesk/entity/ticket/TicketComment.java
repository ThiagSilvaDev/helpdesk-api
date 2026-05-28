package com.thiagsilvadev.helpdesk.entity.ticket;

import com.thiagsilvadev.helpdesk.entity.AuditableEntity;
import com.thiagsilvadev.helpdesk.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "ticket_comments")
public class TicketComment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    public TicketComment() {}

    public TicketComment(Ticket ticket, User author, String content) {
        this.ticket = Objects.requireNonNull(ticket, "ticket must not be null");
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.content = requireContent(content);
    }

    public void updateContent(String content) {
        this.content = requireContent(content);
    }

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    private String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content must not be null or blank");
        }

        return content.strip();
    }
}
