package io.powerscale.ranking.scale;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.events.ScaleVoteSubmittedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ScaleVoteSubmittedConsumer {

    private final ObjectMapper objectMapper;
    private final ScaleVoteEventProcessor processor;

    public ScaleVoteSubmittedConsumer(ObjectMapper objectMapper, ScaleVoteEventProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @KafkaListener(
            topics = "${app.kafka.scale-vote-topic:scale-vote-submitted}",
            groupId = "${spring.kafka.consumer.group-id:ranking-service}"
    )
    public void onScaleVoteSubmitted(String payload) throws Exception {
        ScaleVoteSubmittedEvent event = objectMapper.readValue(payload, ScaleVoteSubmittedEvent.class);
        processor.process(event);
    }
}
