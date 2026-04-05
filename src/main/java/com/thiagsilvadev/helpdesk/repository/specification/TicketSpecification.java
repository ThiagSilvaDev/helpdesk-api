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
        TicketSearchCriteria safeCriteria = criteria == null
                ? new TicketSearchCriteria(null, null)
                : criteria;

        return Specification.where(all())
                .and(hasStatus(safeCriteria.status()))
                .and(hasPriority(safeCriteria.priority()));
    }

    private static Specification<Ticket> hasStatus(TicketStatus status) {
        return status == null
                ? all()
                : (root, query, cb) -> cb.equal(root.get(STATUS_FIELD), status);
    }

    private static Specification<Ticket> hasPriority(TicketPriority priority) {
        return priority == null
                ? all()
                : (root, query, cb) -> cb.equal(root.get(PRIORITY_FIELD), priority);
    }

    private static Specification<Ticket> all() {
        return (root, query, cb) -> cb.conjunction();
    }
}
