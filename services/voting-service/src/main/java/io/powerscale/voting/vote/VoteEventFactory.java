package io.powerscale.voting.vote;

import io.powerscale.events.VoteSubmittedEvent;
import io.powerscale.voting.character.Character;
import io.powerscale.voting.matchup.Matchup;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class VoteEventFactory {

    public VoteSubmittedEvent create(UUID eventId, Vote vote, Instant occurredAt) {
        Matchup matchup = vote.getMatchup();
        Character winner = vote.getWinner();
        Character loser = matchup.getCharacterA().getId().equals(winner.getId())
                ? matchup.getCharacterB()
                : matchup.getCharacterA();

        return new VoteSubmittedEvent(
                eventId,
                vote.getId(),
                matchup.getId(),
                vote.getUserId(),
                snapshot(winner),
                snapshot(loser),
                vote.getDifficulty().name(),
                vote.getVersion(),
                occurredAt
        );
    }

    private VoteSubmittedEvent.CharacterSnapshot snapshot(Character character) {
        return new VoteSubmittedEvent.CharacterSnapshot(
                character.getId(),
                character.getSource().name(),
                character.getExternalId(),
                character.getMalId(),
                character.getName(),
                character.getImageUrl()
        );
    }
}
