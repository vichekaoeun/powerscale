package io.powerscale.voting.scale;

import io.powerscale.events.RankingFormula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scales")
public class Scale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 120)
    private String criterion;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "creator_key_hash", nullable = false, length = 64)
    private String creatorKeyHash;

    @Column(name = "ranking_method", nullable = false, length = 20)
    private String rankingMethod;

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

    @Column(name = "selection1", length = 40)
    private String selection1;

    @Column(name = "selection2", length = 40)
    private String selection2;

    @Column(name = "selection3", length = 40)
    private String selection3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Scale() {
    }

    public Scale(
            String slug,
            String title,
            String criterion,
            String description,
            String creatorKeyHash,
            RankingFormula formula,
            java.util.List<String> selections
    ) {
        this.slug = slug;
        this.title = title;
        this.criterion = criterion;
        this.description = description;
        this.creatorKeyHash = creatorKeyHash;
        this.rankingMethod = formula.method();
        this.baseRating = formula.baseRating();
        this.kFactor = formula.kFactor();
        this.lowWeight = formula.lowWeight();
        this.midWeight = formula.midWeight();
        this.highWeight = formula.highWeight();
        this.lowPoints = formula.lowPoints();
        this.midPoints = formula.midPoints();
        this.highPoints = formula.highPoints();
        this.selection1 = selections.size() > 0 ? selections.get(0) : null;
        this.selection2 = selections.size() > 1 ? selections.get(1) : null;
        this.selection3 = selections.size() > 2 ? selections.get(2) : null;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getCriterion() {
        return criterion;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatorKeyHash() {
        return creatorKeyHash;
    }

    public RankingFormula getFormula() {
        return new RankingFormula(
                rankingMethod,
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

    public java.util.List<String> getSelections() {
        java.util.List<String> selections = new java.util.ArrayList<>();
        if (selection1 != null && !selection1.isBlank()) {
            selections.add(selection1);
        }
        if (selection2 != null && !selection2.isBlank()) {
            selections.add(selection2);
        }
        if (selection3 != null && !selection3.isBlank()) {
            selections.add(selection3);
        }
        return selections;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
