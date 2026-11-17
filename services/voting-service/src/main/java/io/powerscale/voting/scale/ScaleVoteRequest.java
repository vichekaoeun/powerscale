package io.powerscale.voting.scale;

import io.powerscale.voting.vote.Difficulty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScaleVoteRequest(
        @NotNull UUID matchupId,
        @NotNull UUID winnerId,
        @NotNull Difficulty difficulty
) {
}
