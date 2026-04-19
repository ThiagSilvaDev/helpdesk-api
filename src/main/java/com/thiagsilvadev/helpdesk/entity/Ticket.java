package com.thiagsilvadev.helpdesk.entity;

import com.thiagsilvadev.helpdesk.exception.InvalidRoleAssignmentException;
import com.thiagsilvadev.helpdesk.exception.InvalidTicketStateException;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant closedAt;

    public Ticket() {
    }

    public Ticket(String title, String description, User client, TicketPriority priority) {
        if (client.getRole() != Roles.ROLE_USER) {
            throw new InvalidRoleAssignmentException("Only users with ROLE_USER can open a ticket");
        }
        this.title = title;
        this.description = description;
        this.status = TicketStatus.OPEN;
        this.priority = priority != null ? priority : TicketPriority.TRIAGE;
        this.client = client;
    }

    public void update(String title, String description) {
        if (this.status == TicketStatus.CLOSED || this.status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Cannot update a " + this.status + " ticket");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or blank");
        }

        this.title = title;
        this.description = description;
    }

    public void changePriority(TicketPriority priority) {
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Cannot change priority of a closed ticket");
        }

        if (this.priority == priority) {
            return;
        }

        this.priority = priority;
    }

    public void assignTechnician(User technician) {
        if (technician.getRole() != Roles.ROLE_TECHNICIAN) {
            throw new InvalidRoleAssignmentException("Assigned user must have TECHNICIAN role");
        }

        if (this.status == TicketStatus.CLOSED || this.status == TicketStatus.CANCELLED) {
            throw new InvalidRoleAssignmentException("Cannot assign technician to a " + this.status + " ticket");
        }

        this.technician = technician;
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void closeTicket() {
        if (this.status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Ticket is already closed");
        }
        if (this.status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Cannot close a cancelled ticket");
        }
        this.status = TicketStatus.CLOSED;
        this.closedAt = Instant.now();
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

    public String getDescription() {
        return description;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}
