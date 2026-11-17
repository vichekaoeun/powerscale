package io.powerscale.ranking.event;

import io.powerscale.events.VoteSubmittedEvent;
import io.powerscale.ranking.character.RankedCharacter;
import io.powerscale.ranking.character.RankedCharacterRepository;
import io.powerscale.ranking.leaderboard.LeaderboardCache;
import io.powerscale.ranking.rating.Difficulty;
import io.powerscale.ranking.rating.RatingCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VoteEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(VoteEventProcessor.class);

    private final ProcessedEventRepository processedEventRepository;
    private final RankedVoteRepository rankedVoteRepository;
    private final RankedCharacterRepository characterRepository;
    private final RatingCalculator ratingCalculator;
    private final LeaderboardCache leaderboardCache;

    public VoteEventProcessor(
            ProcessedEventRepository processedEventRepository,
            RankedVoteRepository rankedVoteRepository,
            RankedCharacterRepository characterRepository,
            RatingCalculator ratingCalculator,
            LeaderboardCache leaderboardCache
    ) {
        this.processedEventRepository = processedEventRepository;
        this.rankedVoteRepository = rankedVoteRepository;
        this.characterRepository = characterRepository;
        this.ratingCalculator = ratingCalculator;
        this.leaderboardCache = leaderboardCache;
    }

    @Transactional
    public void process(VoteSubmittedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.debug("Skipping duplicate event {}", event.eventId());
            return;
        }

        RankedVote existing = rankedVoteRepository.findById(event.voteId()).orElse(null);
        if (existing != null && existing.getVoteVersion() >= event.version()) {
            processedEventRepository.save(new ProcessedEvent(event.eventId()));
            return;
        }

        upsertCharacter(event.winner());
        upsertCharacter(event.loser());

        if (existing == null) {
            rankedVoteRepository.save(new RankedVote(
                    event.voteId(),
                    event.matchupId(),
                    event.userId(),
                    event.winner().id(),
                    event.loser().id(),
                    event.difficulty(),
                    event.version(),
                    event.occurredAt()
            ));
        } else {
            existing.update(
                    event.matchupId(),
                    event.userId(),
                    event.winner().id(),
                    event.loser().id(),
                    event.difficulty(),
                    event.version(),
                    event.occurredAt()
            );
            rankedVoteRepository.save(existing);
        }

        recomputeRatings();
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        leaderboardCache.invalidate();
    }

    private void upsertCharacter(VoteSubmittedEvent.CharacterSnapshot snapshot) {
        RankedCharacter character = characterRepository.findById(snapshot.id()).orElse(null);
        if (character == null) {
            character = new RankedCharacter(
                    snapshot.id(),
                    snapshot.source(),
                    snapshot.externalId(),
                    snapshot.malId(),
                    snapshot.name(),
                    snapshot.imageUrl(),
                    RatingCalculator.BASE_RATING
            );
        } else {
            character.updateMetadata(
                    snapshot.source(),
                    snapshot.externalId(),
                    snapshot.malId(),
                    snapshot.name(),
                    snapshot.imageUrl()
            );
        }
        characterRepository.save(character);
    }

    private void recomputeRatings() {
        Map<UUID, RankedCharacter> characters = new HashMap<>();
        for (RankedCharacter character : characterRepository.findAll()) {
            character.resetRating(RatingCalculator.BASE_RATING);
            characters.put(character.getCharacterId(), character);
        }

        List<RankedVote> votes = rankedVoteRepository.findAllByOrderByOccurredAtAscVoteIdAsc();
        for (RankedVote vote : votes) {
            RankedCharacter winner = characters.get(vote.getWinnerId());
            RankedCharacter loser = characters.get(vote.getLoserId());
            if (winner == null || loser == null) {
                continue;
            }

            RatingCalculator.RatingUpdate update = ratingCalculator.calculate(
                    winner.getRating(),
                    loser.getRating(),
                    Difficulty.valueOf(vote.getDifficulty())
            );
            winner.applyWin(update.winnerDelta());
            loser.applyLoss(update.loserDelta());
        }

        characterRepository.saveAll(characters.values());
    }
}
