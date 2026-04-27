package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    boolean existsByIdAndAuthorId(Long id, Long authorId);

    Optional<TicketComment> findByIdAndTicketId(Long id, Long ticketId);

    Page<TicketComment> findByTicketId(Long ticketId, Pageable pageable);
}
