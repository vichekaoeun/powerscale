package io.powerscale.ranking.scale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScaleProcessedEventRepository extends JpaRepository<ScaleProcessedEvent, UUID> {
}
