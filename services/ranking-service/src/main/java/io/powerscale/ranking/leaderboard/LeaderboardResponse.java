package io.powerscale.ranking.leaderboard;

import java.util.List;

public record LeaderboardResponse(
        List<LeaderboardEntry> entries,
        long totalCharacters,
        long totalVotes
) {
}
