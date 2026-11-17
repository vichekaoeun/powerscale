package io.powerscale.ranking.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.events.VoteSubmittedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VoteSubmittedConsumer {

    private final ObjectMapper objectMapper;
    private final VoteEventProcessor voteEventProcessor;

    public VoteSubmittedConsumer(ObjectMapper objectMapper, VoteEventProcessor voteEventProcessor) {
        this.objectMapper = objectMapper;
        this.voteEventProcessor = voteEventProcessor;
    }

    @KafkaListener(
            topics = "${app.kafka.vote-topic:vote-submitted}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void onVoteSubmitted(String payload) throws Exception {
        VoteSubmittedEvent event = objectMapper.readValue(payload, VoteSubmittedEvent.class);
        voteEventProcessor.process(event);
    }
}
