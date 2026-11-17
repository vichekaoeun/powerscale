package io.powerscale.voting.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboxEvent o where o.publishedAt is null order by o.createdAt asc")
    List<OutboxEvent> findUnpublished(Pageable pageable);
}
