package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxEvent;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationOutboxStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select event
              from NotificationOutboxEvent event
             where event.status in :statuses
               and event.nextAttemptAt <= :now
             order by event.createdAt asc
            """)
    List<NotificationOutboxEvent> findPublishable(
            List<NotificationOutboxStatus> statuses, Instant now, Pageable pageable);
}
