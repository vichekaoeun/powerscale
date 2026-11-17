package io.powerscale.voting.scale;

import io.powerscale.events.RankingFormula;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScaleResponse(
        UUID id,
        String slug,
        String title,
        String criterion,
        String description,
        RankingFormula formula,
        List<String> selections,
        int itemCount,
        List<ScaleItemResponse> items,
        Instant createdAt
) {
    public static ScaleResponse from(Scale scale, List<ScaleItem> items) {
        return new ScaleResponse(
                scale.getId(),
                scale.getSlug(),
                scale.getTitle(),
                scale.getCriterion(),
                scale.getDescription(),
                scale.getFormula(),
                scale.getSelections(),
                items.size(),
                items.stream().map(ScaleItemResponse::from).toList(),
                scale.getCreatedAt()
        );
    }
}
