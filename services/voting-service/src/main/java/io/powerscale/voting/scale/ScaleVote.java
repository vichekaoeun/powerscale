package io.powerscale.voting.scale;

import io.powerscale.voting.vote.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "scale_votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_scale_votes_user_matchup",
                columnNames = {"user_id", "matchup_id"}
        )
)
public class ScaleVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scale_id", nullable = false)
    private UUID scaleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matchup_id", nullable = false)
    private ScaleMatchup matchup;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winner_id", nullable = false)
    private ScaleItem winner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScaleVote() {
    }

    public ScaleVote(
            UUID scaleId,
            ScaleMatchup matchup,
            String userId,
            ScaleItem winner,
            Difficulty difficulty
    ) {
        this.scaleId = scaleId;
        this.matchup = matchup;
        this.userId = userId;
        this.winner = winner;
        this.difficulty = difficulty;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(ScaleItem winner, Difficulty difficulty) {
        this.winner = winner;
        this.difficulty = difficulty;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getScaleId() {
        return scaleId;
    }

    public ScaleMatchup getMatchup() {
        return matchup;
    }

    public String getUserId() {
        return userId;
    }

    public ScaleItem getWinner() {
        return winner;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
