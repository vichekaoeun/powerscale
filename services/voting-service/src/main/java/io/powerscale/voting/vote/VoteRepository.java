package io.powerscale.voting.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByUserIdAndMatchupId(String userId, UUID matchupId);

    @Query("""
            select v from Vote v
            join fetch v.matchup m
            join fetch m.characterA
            join fetch m.characterB
            join fetch v.winner
            order by v.createdAt asc, v.id asc
            """)
    List<Vote> findAllWithDetails();

    @Query("""
            select v from Vote v
            join fetch v.matchup m
            join fetch m.characterA
            join fetch m.characterB
            join fetch v.winner
            where not exists (
                select 1 from OutboxEvent o
                where o.aggregateId = v.id and o.eventType = 'VoteSubmitted'
            )
            order by v.createdAt asc
            """)
    List<Vote> findVotesWithoutOutboxEvent();

    @Query("""
            select v from Vote v
            join fetch v.matchup m
            join fetch m.characterA
            join fetch m.characterB
            join fetch v.winner
            where v.userId = :userId
            order by v.updatedAt desc
            """)
    List<Vote> findAllByUserIdWithDetails(@Param("userId") String userId);

    @Query("""
            select v from Vote v
            join fetch v.matchup m
            join fetch m.characterA
            join fetch m.characterB
            join fetch v.winner
            where v.userId = :userId and v.matchup.id = :matchupId
            """)
    Optional<Vote> findUserVoteWithDetails(
            @Param("userId") String userId,
            @Param("matchupId") UUID matchupId
    );

    @Query("""
            select v.winner.id as winnerId, v.difficulty as difficulty, count(v) as votes
            from Vote v
            where v.matchup.id = :matchupId
            group by v.winner.id, v.difficulty
            """)
    List<OutcomeCount> countOutcomes(@Param("matchupId") UUID matchupId);

    interface OutcomeCount {
        UUID getWinnerId();

        Difficulty getDifficulty();

        long getVotes();
    }
}
