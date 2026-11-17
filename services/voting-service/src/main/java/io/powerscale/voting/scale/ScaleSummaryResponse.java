package io.powerscale.voting.scale;

import java.time.Instant;
import java.util.UUID;

public record ScaleSummaryResponse(
        UUID id,
        String slug,
        String title,
        String criterion,
        String rankingMethod,
        String description,
        int itemCount,
        long voteCount,
        Instant createdAt
) {
}
