package io.powerscale.voting.scale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "scale_matchups",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_scale_matchups_pair",
                columnNames = {"scale_id", "item_a_id", "item_b_id"}
        )
)
public class ScaleMatchup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scale_id", nullable = false)
    private UUID scaleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_a_id", nullable = false)
    private ScaleItem itemA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_b_id", nullable = false)
    private ScaleItem itemB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ScaleMatchup() {
    }

    public ScaleMatchup(UUID scaleId, ScaleItem itemA, ScaleItem itemB) {
        this.scaleId = scaleId;
        this.itemA = itemA;
        this.itemB = itemB;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getScaleId() {
        return scaleId;
    }

    public ScaleItem getItemA() {
        return itemA;
    }

    public ScaleItem getItemB() {
        return itemB;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean includes(UUID itemId) {
        return itemA.getId().equals(itemId) || itemB.getId().equals(itemId);
    }
}
