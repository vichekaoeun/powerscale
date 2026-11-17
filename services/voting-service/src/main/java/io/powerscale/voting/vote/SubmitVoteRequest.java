package io.powerscale.voting.vote;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitVoteRequest(
        @NotNull UUID matchupId,
        @NotNull UUID winnerId,
        @NotNull Difficulty difficulty
) {
}
