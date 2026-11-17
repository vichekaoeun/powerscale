package io.powerscale.ranking.scale;

import io.powerscale.events.RankingFormula;
import io.powerscale.events.ScaleVoteSubmittedEvent;
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
public class ScaleVoteEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScaleVoteEventProcessor.class);

    private final ScaleProcessedEventRepository processedEventRepository;
    private final ScaleRankedVoteRepository voteRepository;
    private final ScaleRankedItemRepository itemRepository;
    private final ScaleRankingConfigRepository configRepository;
    private final RatingCalculator ratingCalculator;

    public ScaleVoteEventProcessor(
            ScaleProcessedEventRepository processedEventRepository,
            ScaleRankedVoteRepository voteRepository,
            ScaleRankedItemRepository itemRepository,
            ScaleRankingConfigRepository configRepository,
            RatingCalculator ratingCalculator
    ) {
        this.processedEventRepository = processedEventRepository;
        this.voteRepository = voteRepository;
        this.itemRepository = itemRepository;
        this.configRepository = configRepository;
        this.ratingCalculator = ratingCalculator;
    }

    @Transactional
    public void process(ScaleVoteSubmittedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.debug("Skipping duplicate scale event {}", event.eventId());
            return;
        }

        ScaleRankedVote existing = voteRepository.findById(event.voteId()).orElse(null);
        if (existing != null && existing.getVoteVersion() >= event.version()) {
            processedEventRepository.save(new ScaleProcessedEvent(event.eventId()));
            return;
        }

        RankingFormula formula = upsertFormula(event.scaleId(), event.formula());
        upsertItem(event.scaleId(), event.winner());
        upsertItem(event.scaleId(), event.loser());

        if (existing == null) {
            voteRepository.save(new ScaleRankedVote(
                    event.voteId(),
                    event.scaleId(),
                    event.userId(),
                    event.winner().id(),
                    event.loser().id(),
                    event.difficulty(),
                    event.version(),
                    event.occurredAt()
            ));
        } else {
            existing.update(
                    event.scaleId(),
                    event.userId(),
                    event.winner().id(),
                    event.loser().id(),
                    event.difficulty(),
                    event.version(),
                    event.occurredAt()
            );
            voteRepository.save(existing);
        }

        recompute(event.scaleId(), formula);
        processedEventRepository.save(new ScaleProcessedEvent(event.eventId()));
    }

    private RankingFormula upsertFormula(UUID scaleId, RankingFormula eventFormula) {
        ScaleRankingConfig config = configRepository.findById(scaleId).orElse(null);
        if (eventFormula != null) {
            if (config == null) {
                config = new ScaleRankingConfig(scaleId, eventFormula);
            } else {
                config.apply(eventFormula);
            }
            configRepository.save(config);
        } else if (config == null) {
            config = new ScaleRankingConfig(scaleId, RankingFormula.eloDefault());
            configRepository.save(config);
        }
        return config.toFormula();
    }

    private void upsertItem(UUID scaleId, ScaleVoteSubmittedEvent.ScaleItemSnapshot snapshot) {
        ScaleRankedItem item = itemRepository.findById(snapshot.id()).orElse(null);
        if (item == null) {
            item = new ScaleRankedItem(
                    snapshot.id(),
                    scaleId,
                    snapshot.name(),
                    snapshot.imageUrl(),
                    RatingCalculator.BASE_RATING
            );
        } else {
            item.updateMetadata(snapshot.name(), snapshot.imageUrl());
        }
        itemRepository.save(item);
    }

    private void recompute(UUID scaleId, RankingFormula formula) {
        List<ScaleRankedItem> items = itemRepository.findAllByScaleId(scaleId);
        Map<UUID, ScaleRankedItem> itemsById = new HashMap<>();
        double startingRating = formula.isPoints() ? 0.0 : formula.baseRating();
        for (ScaleRankedItem item : items) {
            item.resetRating(startingRating);
            itemsById.put(item.getItemId(), item);
        }

        List<ScaleRankedVote> votes = voteRepository.findAllByScaleIdOrderByOccurredAtAscVoteIdAsc(scaleId);
        for (ScaleRankedVote vote : votes) {
            ScaleRankedItem winner = itemsById.get(vote.getWinnerId());
            ScaleRankedItem loser = itemsById.get(vote.getLoserId());
            if (winner == null || loser == null) {
                continue;
            }

            RatingCalculator.RatingUpdate update = ratingCalculator.calculate(
                    formula,
                    winner.getRating(),
                    loser.getRating(),
                    Difficulty.valueOf(vote.getDifficulty())
            );
            winner.applyWin(update.winnerDelta());
            loser.applyLoss(update.loserDelta());
        }

        itemRepository.saveAll(items);
    }
}
