package io.powerscale.voting.matchup;

import io.powerscale.voting.character.Character;
import io.powerscale.voting.character.CharacterRepository;
import io.powerscale.voting.character.CharacterSummary;
import io.powerscale.voting.vote.Difficulty;
import io.powerscale.voting.vote.VoteRepository;
import io.powerscale.voting.web.ConflictException;
import io.powerscale.voting.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MatchupService {

    private final MatchupRepository matchupRepository;
    private final CharacterRepository characterRepository;
    private final VoteRepository voteRepository;

    public MatchupService(
            MatchupRepository matchupRepository,
            CharacterRepository characterRepository,
            VoteRepository voteRepository
    ) {
        this.matchupRepository = matchupRepository;
        this.characterRepository = characterRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public MatchupResponse getOrCreateRandomMatchup() {
        List<UUID> characterIds = characterRepository.findAllIds();
        if (characterIds.size() < 2) {
            throw new ConflictException("At least two characters are required before matchups can be generated");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int firstIndex = random.nextInt(characterIds.size());
        int secondIndex = random.nextInt(characterIds.size() - 1);
        if (secondIndex >= firstIndex) {
            secondIndex++;
        }

        Character first = characterRepository.findById(characterIds.get(firstIndex))
                .orElseThrow(() -> new ResourceNotFoundException("Character not found"));
        Character second = characterRepository.findById(characterIds.get(secondIndex))
                .orElseThrow(() -> new ResourceNotFoundException("Character not found"));

        Character characterA = first;
        Character characterB = second;
        if (characterA.getId().compareTo(characterB.getId()) > 0) {
            characterA = second;
            characterB = first;
        }

        Character finalCharacterA = characterA;
        Character finalCharacterB = characterB;
        Matchup matchup = matchupRepository
                .findByCharacterAIdAndCharacterBId(finalCharacterA.getId(), finalCharacterB.getId())
                .orElseGet(() -> matchupRepository.save(new Matchup(finalCharacterA, finalCharacterB)));

        return MatchupResponse.from(matchup);
    }

    @Transactional(readOnly = true)
    public MatchupResponse get(UUID id) {
        return MatchupResponse.from(findMatchup(id));
    }

    @Transactional(readOnly = true)
    public MatchupStatsResponse stats(UUID id) {
        Matchup matchup = findMatchup(id);
        List<VoteRepository.OutcomeCount> counts = voteRepository.countOutcomes(id);

        Map<OutcomeKey, Long> votesByOutcome = new HashMap<>();
        for (VoteRepository.OutcomeCount count : counts) {
            votesByOutcome.put(new OutcomeKey(count.getWinnerId(), count.getDifficulty()), count.getVotes());
        }

        long totalVotes = votesByOutcome.values().stream().mapToLong(Long::longValue).sum();
        List<OutcomeStat> outcomes = new ArrayList<>();

        for (Character character : List.of(matchup.getCharacterA(), matchup.getCharacterB())) {
            for (Difficulty difficulty : Difficulty.values()) {
                long votes = votesByOutcome.getOrDefault(new OutcomeKey(character.getId(), difficulty), 0L);
                double percentage = totalVotes == 0
                        ? 0.0
                        : Math.round(votes * 10_000.0 / totalVotes) / 100.0;
                outcomes.add(new OutcomeStat(character.getId(), difficulty, votes, percentage));
            }
        }

        return new MatchupStatsResponse(
                matchup.getId(),
                CharacterSummary.from(matchup.getCharacterA()),
                CharacterSummary.from(matchup.getCharacterB()),
                totalVotes,
                outcomes
        );
    }

    Matchup findMatchup(UUID id) {
        return matchupRepository.findWithCharactersById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matchup %s not found".formatted(id)));
    }

    private record OutcomeKey(UUID characterId, Difficulty difficulty) {
    }
}
