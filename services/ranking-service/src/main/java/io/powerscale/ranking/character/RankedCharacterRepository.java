package io.powerscale.ranking.character;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RankedCharacterRepository extends JpaRepository<RankedCharacter, UUID> {
}
