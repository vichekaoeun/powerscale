package io.powerscale.ranking.scale;

import io.powerscale.events.RankingFormula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scale_ranking_configs")
public class ScaleRankingConfig {

    @Id
    @Column(name = "scale_id")
    private UUID scaleId;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(name = "base_rating", nullable = false)
    private double baseRating;

    @Column(name = "k_factor", nullable = false)
    private double kFactor;

    @Column(name = "low_weight", nullable = false)
    private double lowWeight;

    @Column(name = "mid_weight", nullable = false)
    private double midWeight;

    @Column(name = "high_weight", nullable = false)
    private double highWeight;

    @Column(name = "low_points", nullable = false)
    private double lowPoints;

    @Column(name = "mid_points", nullable = false)
    private double midPoints;

    @Column(name = "high_points", nullable = false)
    private double highPoints;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScaleRankingConfig() {
    }

    public ScaleRankingConfig(UUID scaleId, RankingFormula formula) {
        this.scaleId = scaleId;
        apply(formula);
    }

    public void apply(RankingFormula formula) {
        this.method = formula.method();
        this.baseRating = formula.baseRating();
        this.kFactor = formula.kFactor();
        this.lowWeight = formula.lowWeight();
        this.midWeight = formula.midWeight();
        this.highWeight = formula.highWeight();
        this.lowPoints = formula.lowPoints();
        this.midPoints = formula.midPoints();
        this.highPoints = formula.highPoints();
        this.updatedAt = Instant.now();
    }

    public RankingFormula toFormula() {
        return new RankingFormula(
                method,
                baseRating,
                kFactor,
                lowWeight,
                midWeight,
                highWeight,
                lowPoints,
                midPoints,
                highPoints
        );
    }
}
