package io.powerscale.voting.vote;

import io.powerscale.voting.character.Character;
import io.powerscale.voting.matchup.Matchup;
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
        name = "votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_votes_user_matchup",
                columnNames = {"user_id", "matchup_id"}
        )
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matchup_id", nullable = false)
    private Matchup matchup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winner_id", nullable = false)
    private Character winner;

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

    protected Vote() {
    }

    public Vote(String userId, Matchup matchup, Character winner, Difficulty difficulty) {
        this.userId = userId;
        this.matchup = matchup;
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

    public void update(Character winner, Difficulty difficulty) {
        this.winner = winner;
        this.difficulty = difficulty;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Matchup getMatchup() {
        return matchup;
    }

    public Character getWinner() {
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
