package io.powerscale.voting.scale;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ScaleMatchupRepository extends JpaRepository<ScaleMatchup, UUID> {

    @EntityGraph(attributePaths = {"itemA", "itemB"})
    Optional<ScaleMatchup> findByScaleIdAndItemAIdAndItemBId(UUID scaleId, UUID itemAId, UUID itemBId);

    @Query("""
            select m from ScaleMatchup m
            join fetch m.itemA
            join fetch m.itemB
            where m.id = :id
            """)
    Optional<ScaleMatchup> findWithItemsById(@Param("id") UUID id);
}
