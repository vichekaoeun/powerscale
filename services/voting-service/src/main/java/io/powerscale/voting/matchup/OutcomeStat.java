package io.powerscale.voting.matchup;

import io.powerscale.voting.vote.Difficulty;

import java.util.UUID;

public record OutcomeStat(
        UUID characterId,
        Difficulty difficulty,
        long votes,
        double percentage
) {
}
