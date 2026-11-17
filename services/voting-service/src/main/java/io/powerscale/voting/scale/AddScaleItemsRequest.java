package io.powerscale.voting.scale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddScaleItemsRequest(
        @NotEmpty @Size(max = 200) List<@Valid ScaleItemRequest> items
) {
}
