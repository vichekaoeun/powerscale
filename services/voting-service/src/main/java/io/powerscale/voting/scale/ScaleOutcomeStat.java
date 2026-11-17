package io.powerscale.voting.scale;

import io.powerscale.voting.vote.Difficulty;

import java.util.UUID;

public record ScaleOutcomeStat(
        UUID itemId,
        Difficulty difficulty,
        long votes,
        double percentage
) {
}
