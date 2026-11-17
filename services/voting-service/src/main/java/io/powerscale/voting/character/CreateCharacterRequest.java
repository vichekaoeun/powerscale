package io.powerscale.voting.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCharacterRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String imageUrl
) {
}
