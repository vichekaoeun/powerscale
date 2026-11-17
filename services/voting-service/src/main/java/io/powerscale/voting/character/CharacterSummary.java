package io.powerscale.voting.character;

import java.util.UUID;

public record CharacterSummary(
        UUID id,
        CharacterSource source,
        long externalId,
        Long malId,
        String name,
        String imageUrl
) {
    public static CharacterSummary from(Character character) {
        return new CharacterSummary(
                character.getId(),
                character.getSource(),
                character.getExternalId(),
                character.getMalId(),
                character.getName(),
                character.getImageUrl()
        );
    }
}
