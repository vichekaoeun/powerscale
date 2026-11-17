package io.powerscale.events;

import java.time.Instant;
import java.util.UUID;

public record VoteSubmittedEvent(
        UUID eventId,
        UUID voteId,
        UUID matchupId,
        String userId,
        CharacterSnapshot winner,
        CharacterSnapshot loser,
        String difficulty,
        long version,
        Instant occurredAt
) {
    public record CharacterSnapshot(
            UUID id,
            String source,
            long externalId,
            Long malId,
            String name,
            String imageUrl
    ) {
    }
}
