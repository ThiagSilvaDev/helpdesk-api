package com.thiagsilvadev.helpdesk.repository.specification;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class TicketSpecificationTest {

    @Test
    void shouldReturnUnrestrictedSpecificationWhenCriteriaIsNull() {
        Specification<Ticket> specification = TicketSpecification.withCriteria(null);

        Predicate predicate = specification.toPredicate(mockRoot(), mockQuery(), mock(CriteriaBuilder.class));

        assertThat(predicate).isNull();
    }

    @Test
    void shouldFilterByStatusAndPriority() {
        Root<Ticket> root = mockRoot();
        CriteriaQuery<?> query = mockQuery();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<Object> statusPath = mockPath();
        Path<Object> priorityPath = mockPath();
        Predicate statusPredicate = mock(Predicate.class);
        Predicate priorityPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        given(root.get("status")).willReturn(statusPath);
        given(root.get("priority")).willReturn(priorityPath);
        given(criteriaBuilder.equal(statusPath, TicketStatus.OPEN)).willReturn(statusPredicate);
        given(criteriaBuilder.equal(priorityPath, TicketPriority.URGENT)).willReturn(priorityPredicate);
        given(criteriaBuilder.and(statusPredicate, priorityPredicate)).willReturn(combinedPredicate);

        Specification<Ticket> specification = TicketSpecification.withCriteria(
                new TicketDTO.Search.Criteria(TicketStatus.OPEN, TicketPriority.URGENT)
        );

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isSameAs(combinedPredicate);
    }

    @Test
    void shouldFilterOnlyByStatusWhenPriorityIsNull() {
        Root<Ticket> root = mockRoot();
        CriteriaQuery<?> query = mockQuery();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<Object> statusPath = mockPath();
        Predicate statusPredicate = mock(Predicate.class);

        given(root.get("status")).willReturn(statusPath);
        given(criteriaBuilder.equal(statusPath, TicketStatus.CLOSED)).willReturn(statusPredicate);

        Specification<Ticket> specification = TicketSpecification.withCriteria(
                new TicketDTO.Search.Criteria(TicketStatus.CLOSED, null)
        );

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isSameAs(statusPredicate);
        verify(root, never()).get("priority");
    }

    @Test
    void shouldFilterOnlyByPriorityWhenStatusIsNull() {
        Root<Ticket> root = mockRoot();
        CriteriaQuery<?> query = mockQuery();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<Object> priorityPath = mockPath();
        Predicate priorityPredicate = mock(Predicate.class);

        given(root.get("priority")).willReturn(priorityPath);
        given(criteriaBuilder.equal(priorityPath, TicketPriority.HIGH)).willReturn(priorityPredicate);

        Specification<Ticket> specification = TicketSpecification.withCriteria(
                new TicketDTO.Search.Criteria(null, TicketPriority.HIGH)
        );

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isSameAs(priorityPredicate);
        verify(root, never()).get("status");
    }

    @SuppressWarnings("unchecked")
    private Root<Ticket> mockRoot() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private CriteriaQuery<?> mockQuery() {
        return mock(CriteriaQuery.class);
    }

    @SuppressWarnings("unchecked")
    private Path<Object> mockPath() {
        return mock(Path.class);
    }
}
