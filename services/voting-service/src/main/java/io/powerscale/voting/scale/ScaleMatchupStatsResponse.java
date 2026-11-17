package io.powerscale.voting.scale;

import java.util.List;
import java.util.UUID;

public record ScaleMatchupStatsResponse(
        UUID matchupId,
        ScaleItemResponse itemA,
        ScaleItemResponse itemB,
        long totalVotes,
        List<ScaleOutcomeStat> outcomes
) {
}
