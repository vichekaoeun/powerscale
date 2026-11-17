package io.powerscale.voting.scale;

import io.powerscale.events.RankingFormula;
import io.powerscale.events.ScaleVoteSubmittedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ScaleVoteEventFactory {

    public ScaleVoteSubmittedEvent create(
            UUID eventId,
            ScaleVote vote,
            RankingFormula formula,
            Instant occurredAt
    ) {
        ScaleMatchup matchup = vote.getMatchup();
        ScaleItem winner = vote.getWinner();
        ScaleItem loser = matchup.getItemA().getId().equals(winner.getId())
                ? matchup.getItemB()
                : matchup.getItemA();

        return new ScaleVoteSubmittedEvent(
                eventId,
                vote.getId(),
                vote.getScaleId(),
                vote.getUserId(),
                snapshot(winner),
                snapshot(loser),
                vote.getDifficulty().name(),
                formula,
                vote.getVersion(),
                occurredAt
        );
    }

    private ScaleVoteSubmittedEvent.ScaleItemSnapshot snapshot(ScaleItem item) {
        return new ScaleVoteSubmittedEvent.ScaleItemSnapshot(
                item.getId(),
                item.getName(),
                item.getImageUrl()
        );
    }
}
