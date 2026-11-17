package io.powerscale.events;

import java.time.Instant;
import java.util.UUID;

public record ScaleVoteSubmittedEvent(
        UUID eventId,
        UUID voteId,
        UUID scaleId,
        String userId,
        ScaleItemSnapshot winner,
        ScaleItemSnapshot loser,
        String difficulty,
        RankingFormula formula,
        long version,
        Instant occurredAt
) {
    public ScaleVoteSubmittedEvent(
            UUID eventId,
            UUID voteId,
            UUID scaleId,
            String userId,
            ScaleItemSnapshot winner,
            ScaleItemSnapshot loser,
            String difficulty,
            long version,
            Instant occurredAt
    ) {
        this(
                eventId,
                voteId,
                scaleId,
                userId,
                winner,
                loser,
                difficulty,
                RankingFormula.eloDefault(),
                version,
                occurredAt
        );
    }

    public record ScaleItemSnapshot(
            UUID id,
            String name,
            String imageUrl
    ) {
    }
}
