package io.powerscale.voting.character;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterRepository extends JpaRepository<Character, UUID> {

    Optional<Character> findByMalId(long malId);

    Optional<Character> findBySourceAndExternalId(CharacterSource source, long externalId);

    boolean existsBySourceAndExternalId(CharacterSource source, long externalId);

    boolean existsByNameIgnoreCase(String name);

    Optional<Character> findByNameIgnoreCase(String name);

    Page<Character> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("select c.id from Character c")
    List<UUID> findAllIds();
}
