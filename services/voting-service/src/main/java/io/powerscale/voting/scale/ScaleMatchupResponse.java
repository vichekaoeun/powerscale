package io.powerscale.voting.scale;

import java.time.Instant;
import java.util.UUID;

public record ScaleMatchupResponse(
        UUID id,
        ScaleItemResponse itemA,
        ScaleItemResponse itemB,
        Instant createdAt
) {
    public static ScaleMatchupResponse from(ScaleMatchup matchup) {
        return new ScaleMatchupResponse(
                matchup.getId(),
                ScaleItemResponse.from(matchup.getItemA()),
                ScaleItemResponse.from(matchup.getItemB()),
                matchup.getCreatedAt()
        );
    }
}
