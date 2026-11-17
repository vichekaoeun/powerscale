package io.powerscale.voting.matchup;

import io.powerscale.voting.character.CharacterSummary;

import java.util.List;
import java.util.UUID;

public record MatchupStatsResponse(
        UUID matchupId,
        CharacterSummary characterA,
        CharacterSummary characterB,
        long totalVotes,
        List<OutcomeStat> outcomes
) {
}
