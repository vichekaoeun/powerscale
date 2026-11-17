package io.powerscale.ranking.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RankedVoteRepository extends JpaRepository<RankedVote, UUID> {

    List<RankedVote> findAllByOrderByOccurredAtAscVoteIdAsc();
}
