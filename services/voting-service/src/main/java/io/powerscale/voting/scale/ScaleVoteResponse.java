package io.powerscale.voting.scale;

import io.powerscale.voting.vote.Difficulty;

import java.time.Instant;
import java.util.UUID;

public record ScaleVoteResponse(
        UUID id,
        UUID matchupId,
        ScaleItemResponse winner,
        ScaleItemResponse loser,
        Difficulty difficulty,
        Instant createdAt,
        Instant updatedAt
) {
    public static ScaleVoteResponse from(ScaleVote vote) {
        ScaleMatchup matchup = vote.getMatchup();
        ScaleItem winner = vote.getWinner();
        ScaleItem loser = matchup.getItemA().getId().equals(winner.getId())
                ? matchup.getItemB()
                : matchup.getItemA();

        return new ScaleVoteResponse(
                vote.getId(),
                matchup.getId(),
                ScaleItemResponse.from(winner),
                ScaleItemResponse.from(loser),
                vote.getDifficulty(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }
}
