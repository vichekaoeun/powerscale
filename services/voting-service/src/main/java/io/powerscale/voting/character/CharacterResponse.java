package io.powerscale.voting.character;

import java.util.UUID;

public record CharacterResponse(
        UUID id,
        CharacterSource source,
        long externalId,
        Long malId,
        String name,
        String imageUrl,
        String about
) {
    public static CharacterResponse from(Character character) {
        return new CharacterResponse(
                character.getId(),
                character.getSource(),
                character.getExternalId(),
                character.getMalId(),
                character.getName(),
                character.getImageUrl(),
                character.getAbout()
        );
    }
}
