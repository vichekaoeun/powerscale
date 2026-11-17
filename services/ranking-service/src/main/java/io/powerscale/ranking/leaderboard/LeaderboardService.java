package io.powerscale.ranking.leaderboard;

import io.powerscale.ranking.character.RankedCharacter;
import io.powerscale.ranking.character.RankedCharacterRepository;
import io.powerscale.ranking.event.RankedVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private static final int MAX_LIMIT = 200;

    private final RankedCharacterRepository characterRepository;
    private final RankedVoteRepository voteRepository;
    private final LeaderboardCache cache;

    public LeaderboardService(
            RankedCharacterRepository characterRepository,
            RankedVoteRepository voteRepository,
            LeaderboardCache cache
    ) {
        this.characterRepository = characterRepository;
        this.voteRepository = voteRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }

        return cache.get(limit).orElseGet(() -> {
            LeaderboardResponse response = compute(limit);
            cache.put(limit, response);
            return response;
        });
    }

    private LeaderboardResponse compute(int limit) {
        List<RankedCharacter> characters = new ArrayList<>(characterRepository.findAll());
        characters.sort(this::compare);

        int entryCount = Math.min(limit, characters.size());
        List<LeaderboardEntry> entries = new ArrayList<>(entryCount);
        for (int index = 0; index < entryCount; index++) {
            RankedCharacter character = characters.get(index);
            entries.add(new LeaderboardEntry(
                    index + 1,
                    character.getCharacterId(),
                    character.getSource(),
                    character.getExternalId(),
                    character.getMalId(),
                    character.getName(),
                    character.getImageUrl(),
                    round(character.getRating()),
                    character.getWins(),
                    character.getLosses(),
                    character.getVotes(),
                    winRate(character.getWins(), character.getVotes())
            ));
        }

        return new LeaderboardResponse(entries, characters.size(), voteRepository.count());
    }

    private int compare(RankedCharacter left, RankedCharacter right) {
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
