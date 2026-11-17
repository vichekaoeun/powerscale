package io.powerscale.ranking.scale;

import java.util.List;

public record ScaleLeaderboardResponse(
        List<ScaleLeaderboardEntry> entries,
        long totalItems,
        long totalVotes
) {
}
