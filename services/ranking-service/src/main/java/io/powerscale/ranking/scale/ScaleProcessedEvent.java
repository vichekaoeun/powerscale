package io.powerscale.ranking.scale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scale_processed_events")
public class ScaleProcessedEvent {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ScaleProcessedEvent() {
    }

    public ScaleProcessedEvent(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
