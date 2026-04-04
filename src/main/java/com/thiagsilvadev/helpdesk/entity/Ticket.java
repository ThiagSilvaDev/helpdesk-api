package com.thiagsilvadev.helpdesk.entity;

import com.thiagsilvadev.helpdesk.exception.InvalidTicketStateException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, updatable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private User technician;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    public Ticket() {
    }

    public Ticket(String title, String description, User client) {
        if (client.getRole() != Roles.ROLE_USER) {
            throw new InvalidTicketStateException("Only users with ROLE_USER can open a ticket");
        }
        this.title = title;
        this.description = description;
        this.status = TicketStatus.OPEN;
        this.priority = TicketPriority.TRIAGE;
        this.client = client;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String description) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot update a closed ticket");
        }

        this.title = title;
        this.description = description;
    }

    public void changePriority(TicketPriority priority) {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot change priority of a closed ticket");
        }

        if (this.priority == priority) {
            return;
        }

        this.priority = priority;
    }

    public void closeTicket() {
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Ticket is already closed");
        }
        if (this.status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Cannot close a cancelled ticket");
        }
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void cancelTicket() {
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Cannot cancel a closed ticket");
        }
        if (this.status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Ticket is already cancelled");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public User getClient() {
        return client;
    }

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        if (technician.getRole() != Roles.ROLE_TECHNICIAN) {
            throw new InvalidTicketStateException("Assigned user must have TECHNICIAN role");
        }
        if (this.status == TicketStatus.CLOSED || this.status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Cannot assign technician to a " + this.status + " ticket");
        }
        this.technician = technician;
        this.status = TicketStatus.IN_PROGRESS;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
