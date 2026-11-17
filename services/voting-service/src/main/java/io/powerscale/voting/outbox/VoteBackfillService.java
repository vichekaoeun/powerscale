package io.powerscale.voting.outbox;

import io.powerscale.voting.vote.Vote;
import io.powerscale.voting.vote.VoteEventFactory;
import io.powerscale.voting.vote.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VoteBackfillService {

    private final VoteRepository voteRepository;
    private final OutboxService outboxService;
    private final VoteEventFactory voteEventFactory;

    public VoteBackfillService(
            VoteRepository voteRepository,
            OutboxService outboxService,
            VoteEventFactory voteEventFactory
    ) {
        this.voteRepository = voteRepository;
        this.outboxService = outboxService;
        this.voteEventFactory = voteEventFactory;
    }

    @Transactional
    public int backfill() {
        List<Vote> votes = voteRepository.findVotesWithoutOutboxEvent();
        for (Vote vote : votes) {
            outboxService.enqueueVoteSubmitted(
                    voteEventFactory.create(UUID.randomUUID(), vote, vote.getCreatedAt())
            );
        }
        return votes.size();
    }
}
