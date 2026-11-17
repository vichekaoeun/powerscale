package io.powerscale.voting.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.outbox.publish-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;
    private final String voteTopic;
    private final String scaleVoteTopic;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            TransactionTemplate transactionTemplate,
            @Value("${app.kafka.vote-topic:vote-submitted}") String voteTopic,
            @Value("${app.kafka.scale-vote-topic:scale-vote-submitted}") String scaleVoteTopic
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
        this.voteTopic = voteTopic;
        this.scaleVoteTopic = scaleVoteTopic;
    }

    @Scheduled(fixedDelayString = "${app.outbox.interval-ms:500}")
    public void publishPendingEvents() {
        try {
            transactionTemplate.executeWithoutResult(status -> publishBatch());
        } catch (RuntimeException exception) {
            log.warn("Outbox publish attempt failed; events will be retried", exception);
        }
    }

    private String topicFor(OutboxEvent event) {
        return "ScaleVoteSubmitted".equals(event.getEventType()) ? scaleVoteTopic : voteTopic;
    }

    private void publishBatch() {
        List<OutboxEvent> events = outboxEventRepository.findUnpublished(PageRequest.of(0, BATCH_SIZE));
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate
                        .send(topicFor(event), event.getAggregateId().toString(), event.getPayload())
                        .get(Duration.ofSeconds(10).toMillis(), TimeUnit.MILLISECONDS);
                event.markPublished();
                outboxEventRepository.save(event);
            } catch (Exception exception) {
                throw new IllegalStateException("Could not publish outbox event " + event.getId(), exception);
            }
        }
    }
}
