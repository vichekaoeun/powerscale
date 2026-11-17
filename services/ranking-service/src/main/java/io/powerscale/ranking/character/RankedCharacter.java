package io.powerscale.ranking.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ranked_characters")
public class RankedCharacter {

    @Id
    @Column(name = "character_id")
    private UUID characterId;

    @Column(nullable = false, length = 16)
    private String source;

    @Column(name = "external_id", nullable = false)
    private long externalId;

    @Column(name = "mal_id")
    private Long malId;

    @Column(nullable = false)
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

    protected RankedCharacter() {
    }

    public RankedCharacter(
            UUID characterId,
            String source,
            long externalId,
            Long malId,
            String name,
            String imageUrl,
            double rating
    ) {
        this.characterId = characterId;
        this.source = source;
        this.externalId = externalId;
        this.malId = malId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(
            String source,
            long externalId,
            Long malId,
            String name,
            String imageUrl
    ) {
        this.source = source;
        this.externalId = externalId;
        this.malId = malId;
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

    public UUID getCharacterId() {
        return characterId;
    }

    public String getSource() {
        return source;
    }

    public long getExternalId() {
        return externalId;
    }

    public Long getMalId() {
        return malId;
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
