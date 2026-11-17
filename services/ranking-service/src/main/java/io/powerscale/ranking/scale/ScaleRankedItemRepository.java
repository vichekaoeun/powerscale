package io.powerscale.ranking.scale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScaleRankedItemRepository extends JpaRepository<ScaleRankedItem, UUID> {

    List<ScaleRankedItem> findAllByScaleId(UUID scaleId);
}
