package io.powerscale.ranking.scale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ScaleLeaderboardService {

    private static final int MAX_LIMIT = 200;

    private final ScaleRankedItemRepository itemRepository;
    private final ScaleRankedVoteRepository voteRepository;

    public ScaleLeaderboardService(
            ScaleRankedItemRepository itemRepository,
            ScaleRankedVoteRepository voteRepository
    ) {
        this.itemRepository = itemRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public ScaleLeaderboardResponse getLeaderboard(UUID scaleId, int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }

        List<ScaleRankedItem> items = new ArrayList<>(itemRepository.findAllByScaleId(scaleId));
        items.sort(this::compare);

        int count = Math.min(limit, items.size());
        List<ScaleLeaderboardEntry> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            ScaleRankedItem item = items.get(index);
            entries.add(new ScaleLeaderboardEntry(
                    index + 1,
                    item.getItemId(),
                    item.getName(),
                    item.getImageUrl(),
                    round(item.getRating()),
                    item.getWins(),
                    item.getLosses(),
                    item.getVotes(),
                    winRate(item.getWins(), item.getVotes())
            ));
        }

        return new ScaleLeaderboardResponse(
                entries,
                items.size(),
                voteRepository.countByScaleId(scaleId)
        );
    }

    private int compare(ScaleRankedItem left, ScaleRankedItem right) {
        int byRating = Double.compare(right.getRating(), left.getRating());
        if (byRating != 0) {
            return byRating;
        }
        int byWins = Long.compare(right.getWins(), left.getWins());
        if (byWins != 0) {
            return byWins;
        }
        int byVotes = Long.compare(right.getVotes(), left.getVotes());
        if (byVotes != 0) {
            return byVotes;
        }
        return left.getName().compareToIgnoreCase(right.getName());
    }

    private double winRate(long wins, long votes) {
        return votes == 0 ? 0.0 : round(wins * 100.0 / votes);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
