package io.powerscale.voting.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.events.ScaleVoteSubmittedEvent;
import io.powerscale.events.VoteSubmittedEvent;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void enqueueVoteSubmitted(VoteSubmittedEvent event) {
        enqueue(event.eventId(), "VOTE", event.voteId(), "VoteSubmitted", event);
    }

    public void enqueueScaleVoteSubmitted(ScaleVoteSubmittedEvent event) {
        enqueue(event.eventId(), "SCALE_VOTE", event.voteId(), "ScaleVoteSubmitted", event);
    }

    private void enqueue(
            java.util.UUID eventId,
            String aggregateType,
            java.util.UUID aggregateId,
            String eventType,
            Object event
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(
                    eventId,
                    aggregateType,
                    aggregateId,
                    eventType,
                    payload
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize outbox event", exception);
        }
    }
}
