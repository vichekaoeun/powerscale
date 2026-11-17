package io.powerscale.ranking.scale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scale_ranked_items")
public class ScaleRankedItem {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "scale_id", nullable = false)
    private UUID scaleId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private long wins;

    @Column(nullable = false)
    private long losses;

    @Column(nullable = false)
    private long votes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScaleRankedItem() {
    }

    public ScaleRankedItem(
            UUID itemId,
            UUID scaleId,
            String name,
            String imageUrl,
            double rating
    ) {
        this.itemId = itemId;
        this.scaleId = scaleId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public void resetRating(double rating) {
        this.rating = rating;
        this.wins = 0;
        this.losses = 0;
        this.votes = 0;
    }

    public void applyWin(double delta) {
        this.rating += delta;
        this.wins++;
        this.votes++;
    }

    public void applyLoss(double delta) {
        this.rating += delta;
        this.losses++;
        this.votes++;
    }

    public UUID getItemId() {
        return itemId;
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

    public double getRating() {
        return rating;
    }

    public long getWins() {
        return wins;
    }

    public long getLosses() {
        return losses;
    }

    public long getVotes() {
        return votes;
    }
}
