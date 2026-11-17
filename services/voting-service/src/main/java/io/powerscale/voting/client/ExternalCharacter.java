package io.powerscale.voting.client;

import io.powerscale.voting.character.CharacterSource;

public record ExternalCharacter(
        CharacterSource source,
        long externalId,
        Long malId,
        String name,
        String imageUrl,
        String about,
        Integer favorites
) {
}
