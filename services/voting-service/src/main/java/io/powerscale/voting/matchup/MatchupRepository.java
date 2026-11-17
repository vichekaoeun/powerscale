package io.powerscale.voting.matchup;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MatchupRepository extends JpaRepository<Matchup, UUID> {

    @EntityGraph(attributePaths = {"characterA", "characterB"})
    Optional<Matchup> findByCharacterAIdAndCharacterBId(UUID characterAId, UUID characterBId);

    @Query("""
            select m from Matchup m
            join fetch m.characterA
            join fetch m.characterB
            where m.id = :id
            """)
    Optional<Matchup> findWithCharactersById(@Param("id") UUID id);
}
