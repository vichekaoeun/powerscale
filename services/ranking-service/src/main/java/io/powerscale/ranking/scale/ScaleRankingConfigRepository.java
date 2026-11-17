package io.powerscale.ranking.scale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScaleRankingConfigRepository extends JpaRepository<ScaleRankingConfig, UUID> {
}
