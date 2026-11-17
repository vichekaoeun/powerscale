package io.powerscale.voting.matchup;

import io.powerscale.voting.character.CharacterSummary;

import java.time.Instant;
import java.util.UUID;

public record MatchupResponse(
        UUID id,
        CharacterSummary characterA,
        CharacterSummary characterB,
        Instant createdAt
) {
    public static MatchupResponse from(Matchup matchup) {
        return new MatchupResponse(
                matchup.getId(),
                CharacterSummary.from(matchup.getCharacterA()),
                CharacterSummary.from(matchup.getCharacterB()),
                matchup.getCreatedAt()
        );
    }
}
