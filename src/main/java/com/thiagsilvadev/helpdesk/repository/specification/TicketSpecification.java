package com.thiagsilvadev.helpdesk.repository.specification;

import com.thiagsilvadev.helpdesk.dto.ticket.TicketSearchCriteria;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecification {

    private static final String STATUS_FIELD = "status";
    private static final String PRIORITY_FIELD = "priority";

    private TicketSpecification() {
    }

    public static Specification<Ticket> withCriteria(TicketSearchCriteria criteria) {
        if (criteria == null) {
            return Specification.unrestricted();
        }

        return Specification.where(hasStatus(criteria.status()))
                .and(hasPriority(criteria.priority()));
    }

    private static Specification<Ticket> hasStatus(TicketStatus status) {
        return status == null
                ? Specification.unrestricted()
                : (root, query, cb) -> cb.equal(root.get(STATUS_FIELD), status);
    }

    private static Specification<Ticket> hasPriority(TicketPriority priority) {
        return priority == null
                ? Specification.unrestricted()
                : (root, query, cb) -> cb.equal(root.get(PRIORITY_FIELD), priority);
    }
}
