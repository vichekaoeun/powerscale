package io.powerscale.voting.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.backfill.enabled", havingValue = "true", matchIfMissing = true)
public class VoteBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VoteBackfillRunner.class);

    private final VoteBackfillService voteBackfillService;

    public VoteBackfillRunner(VoteBackfillService voteBackfillService) {
        this.voteBackfillService = voteBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int backfilled = voteBackfillService.backfill();
        if (backfilled > 0) {
            log.info("Queued {} existing votes for Kafka publishing", backfilled);
        }
    }
}
