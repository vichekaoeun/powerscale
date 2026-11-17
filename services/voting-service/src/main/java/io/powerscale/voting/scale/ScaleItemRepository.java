package io.powerscale.voting.scale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScaleItemRepository extends JpaRepository<ScaleItem, UUID> {

    List<ScaleItem> findByScaleIdOrderByNameAsc(UUID scaleId);

    @Query("select i.id from ScaleItem i where i.scaleId = :scaleId")
    List<UUID> findAllIdsByScaleId(@Param("scaleId") UUID scaleId);

    boolean existsByScaleIdAndNameIgnoreCase(UUID scaleId, String name);

    long countByScaleId(UUID scaleId);
}
