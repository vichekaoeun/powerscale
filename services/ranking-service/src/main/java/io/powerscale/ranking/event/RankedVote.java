package io.powerscale.ranking.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ranked_votes")
public class RankedVote {

    @Id
    @Column(name = "vote_id")
    private UUID voteId;

    @Column(name = "matchup_id", nullable = false)
    private UUID matchupId;

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

    protected RankedVote() {
    }

    public RankedVote(
            UUID voteId,
            UUID matchupId,
            String userId,
            UUID winnerId,
            UUID loserId,
            String difficulty,
            long voteVersion,
            Instant occurredAt
    ) {
        this.voteId = voteId;
        this.matchupId = matchupId;
        this.userId = userId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.difficulty = difficulty;
        this.voteVersion = voteVersion;
        this.occurredAt = occurredAt;
    }

    public void update(
            UUID matchupId,
            String userId,
            UUID winnerId,
            UUID loserId,
            String difficulty,
            long voteVersion,
            Instant occurredAt
    ) {
        this.matchupId = matchupId;
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
