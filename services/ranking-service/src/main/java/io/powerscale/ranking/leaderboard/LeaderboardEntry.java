package io.powerscale.ranking.leaderboard;

import java.util.UUID;

public record LeaderboardEntry(
        int rank,
        UUID id,
        String source,
        long externalId,
        Long malId,
        String name,
        String imageUrl,
        double rating,
        long wins,
        long losses,
        long votes,
        double winRate
) {
}
