package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadAtIsNull(Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    boolean existsBySourceEventId(UUID sourceEventId);

    @Modifying
    @Query("""
            update Notification n
               set n.readAt = :readAt
             where n.recipient.id = :recipientId
               and n.readAt is null
            """)
    int markAllAsRead(Long recipientId, Instant readAt);
}
