package io.powerscale.ranking.scale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scale_ranked_votes")
public class ScaleRankedVote {

    @Id
    @Column(name = "vote_id")
    private UUID voteId;

    @Column(name = "scale_id", nullable = false)
    private UUID scaleId;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "winner_id", nullable = false)
    private UUID winnerId;

    @Column(name = "loser_id", nullable = false)
    private UUID loserId;

    @Column(nullable = false, length = 10)
    private String difficulty;

    @Column(name = "vote_version", nullable = false)
    private long voteVersion;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ScaleRankedVote() {
    }

    public ScaleRankedVote(
            UUID voteId,
            UUID scaleId,
            String userId,
            UUID winnerId,
            UUID loserId,
            String difficulty,
            long voteVersion,
            Instant occurredAt
    ) {
        this.voteId = voteId;
        this.scaleId = scaleId;
        this.userId = userId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.difficulty = difficulty;
        this.voteVersion = voteVersion;
        this.occurredAt = occurredAt;
    }

    public void update(
            UUID scaleId,
            String userId,
            UUID winnerId,
            UUID loserId,
            String difficulty,
            long voteVersion,
            Instant occurredAt
    ) {
        this.scaleId = scaleId;
        this.userId = userId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.difficulty = difficulty;
        this.voteVersion = voteVersion;
        this.occurredAt = occurredAt;
    }

    public UUID getVoteId() {
        return voteId;
    }

    public UUID getScaleId() {
        return scaleId;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public UUID getLoserId() {
        return loserId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public long getVoteVersion() {
        return voteVersion;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
