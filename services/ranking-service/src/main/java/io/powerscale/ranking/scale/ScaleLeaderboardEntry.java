package io.powerscale.ranking.scale;

import java.util.UUID;

public record ScaleLeaderboardEntry(
        int rank,
        UUID id,
        String name,
        String imageUrl,
        double rating,
        long wins,
        long losses,
        long votes,
        double winRate
) {
}
