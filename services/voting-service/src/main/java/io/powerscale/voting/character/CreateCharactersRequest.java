package io.powerscale.voting.character;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCharactersRequest(
        @NotEmpty @Size(max = 50) List<@Valid CreateCharacterRequest> characters
) {
}
