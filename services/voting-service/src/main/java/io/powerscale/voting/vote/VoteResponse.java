package io.powerscale.voting.vote;

import io.powerscale.voting.character.Character;
import io.powerscale.voting.character.CharacterSummary;
import io.powerscale.voting.matchup.Matchup;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
        UUID id,
        UUID matchupId,
        CharacterSummary winner,
        CharacterSummary loser,
        Difficulty difficulty,
        Instant createdAt,
        Instant updatedAt
) {
    public static VoteResponse from(Vote vote) {
        Matchup matchup = vote.getMatchup();
        Character winner = vote.getWinner();
        Character loser = matchup.getCharacterA().getId().equals(winner.getId())
                ? matchup.getCharacterB()
                : matchup.getCharacterA();

        return new VoteResponse(
                vote.getId(),
                matchup.getId(),
                CharacterSummary.from(winner),
                CharacterSummary.from(loser),
                vote.getDifficulty(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }
}
