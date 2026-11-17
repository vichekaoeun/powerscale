package io.powerscale.ranking.scale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScaleRankedVoteRepository extends JpaRepository<ScaleRankedVote, UUID> {

    List<ScaleRankedVote> findAllByScaleIdOrderByOccurredAtAscVoteIdAsc(UUID scaleId);

    long countByScaleId(UUID scaleId);
}
