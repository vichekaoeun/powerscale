package io.powerscale.voting.scale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scale_items")
public class ScaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scale_id", nullable = false)
    private UUID scaleId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ScaleItem() {
    }

    public ScaleItem(UUID scaleId, String name, String imageUrl) {
        this.scaleId = scaleId;
        this.name = name;
        this.imageUrl = imageUrl;
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

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
