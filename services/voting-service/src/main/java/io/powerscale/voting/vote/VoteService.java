package io.powerscale.voting.vote;

import io.powerscale.voting.character.Character;
import io.powerscale.voting.matchup.Matchup;
import io.powerscale.voting.matchup.MatchupRepository;
import io.powerscale.voting.outbox.OutboxService;
import io.powerscale.voting.web.BadRequestException;
import io.powerscale.voting.web.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VoteService {

    private static final int MAX_USER_ID_LENGTH = 128;
    private static final int MAX_WRITE_ATTEMPTS = 3;

    private final VoteRepository voteRepository;
    private final MatchupRepository matchupRepository;
    private final OutboxService outboxService;
    private final VoteEventFactory voteEventFactory;
    private final TransactionTemplate transactionTemplate;

    public VoteService(
            VoteRepository voteRepository,
            MatchupRepository matchupRepository,
            OutboxService outboxService,
            VoteEventFactory voteEventFactory,
            TransactionTemplate transactionTemplate
    ) {
        this.voteRepository = voteRepository;
        this.matchupRepository = matchupRepository;
        this.outboxService = outboxService;
        this.voteEventFactory = voteEventFactory;
        this.transactionTemplate = transactionTemplate;
    }

    public VoteResponse submit(String userId, SubmitVoteRequest request) {
        String normalizedUserId = normalizeUserId(userId);

        for (int attempt = 1; ; attempt++) {
            try {
                VoteResponse response = transactionTemplate.execute(
                        status -> doSubmit(normalizedUserId, request)
                );
                if (response == null) {
                    throw new IllegalStateException("Vote transaction returned no result");
                }
                return response;
            } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException exception) {
                if (attempt >= MAX_WRITE_ATTEMPTS) {
                    throw exception;
                }
                backoff(attempt);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<VoteResponse> getUserVotes(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        return voteRepository.findAllByUserIdWithDetails(normalizedUserId).stream()
                .map(VoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<VoteResponse> getUserVote(String userId, UUID matchupId) {
        String normalizedUserId = normalizeUserId(userId);
        return voteRepository.findUserVoteWithDetails(normalizedUserId, matchupId)
                .map(VoteResponse::from);
    }

    private VoteResponse doSubmit(String userId, SubmitVoteRequest request) {
        Matchup matchup = matchupRepository.findWithCharactersById(request.matchupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Matchup %s not found".formatted(request.matchupId())));

        if (!matchup.includes(request.winnerId())) {
            throw new BadRequestException("winnerId must be one of the matchup's characters");
        }

        Character winner = matchup.getCharacterA().getId().equals(request.winnerId())
                ? matchup.getCharacterA()
                : matchup.getCharacterB();

        Vote vote = voteRepository.findByUserIdAndMatchupId(userId, matchup.getId()).orElse(null);
        if (vote == null) {
            vote = new Vote(userId, matchup, winner, request.difficulty());
        } else {
            vote.update(winner, request.difficulty());
        }

        Vote saved = voteRepository.saveAndFlush(vote);
        outboxService.enqueueVoteSubmitted(voteEventFactory.create(UUID.randomUUID(), saved, Instant.now()));

        return VoteResponse.from(saved);
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(25L * attempt);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying vote", interrupted);
        }
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("X-User-Id header must not be blank");
        }
        String normalized = userId.trim();
        if (normalized.length() > MAX_USER_ID_LENGTH) {
            throw new BadRequestException("X-User-Id header must be at most " + MAX_USER_ID_LENGTH + " characters");
        }
        return normalized;
    }
}
