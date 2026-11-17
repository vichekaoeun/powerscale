package io.powerscale.voting.scale;

import io.powerscale.voting.vote.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScaleVoteRepository extends JpaRepository<ScaleVote, UUID> {

    Optional<ScaleVote> findByUserIdAndMatchupId(String userId, UUID matchupId);

    @Query("""
            select v from ScaleVote v
            join fetch v.matchup m
            join fetch m.itemA
            join fetch m.itemB
            join fetch v.winner
            where v.userId = :userId and v.matchup.id = :matchupId
            """)
    Optional<ScaleVote> findUserVoteWithDetails(
            @Param("userId") String userId,
            @Param("matchupId") UUID matchupId
    );

    @Query("""
            select v.winner.id as winnerId, v.difficulty as difficulty, count(v) as votes
            from ScaleVote v
            where v.matchup.id = :matchupId
            group by v.winner.id, v.difficulty
            """)
    List<OutcomeCount> countOutcomes(@Param("matchupId") UUID matchupId);

    long countByScaleId(UUID scaleId);

    interface OutcomeCount {
        UUID getWinnerId();

        Difficulty getDifficulty();

        long getVotes();
    }
}
