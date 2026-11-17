package io.powerscale.voting.scale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScaleItemRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String imageUrl
) {
}
