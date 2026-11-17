package io.powerscale.voting.scale;

import io.powerscale.events.RankingFormula;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateScaleRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 120) String criterion,
        @Size(max = 1000) String description,
        @NotNull @Size(min = 2, max = 200) List<@Valid ScaleItemRequest> items,
        RankingFormula formula,
        @Size(max = 3) List<@NotBlank @Size(max = 40) String> selections
) {
}
