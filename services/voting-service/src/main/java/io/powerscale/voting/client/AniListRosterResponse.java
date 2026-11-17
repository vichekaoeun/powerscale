package io.powerscale.voting.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AniListRosterResponse(
        AniListData data,
        List<AniListError> errors
) {
    public record AniListData(@JsonProperty("Media") AniListMedia media) {
    }

    public record AniListMedia(AniListCharacters characters) {
    }

    public record AniListCharacters(AniListPageInfo pageInfo, List<AniListCharacter> nodes) {
    }

    public record AniListPageInfo(boolean hasNextPage, int total) {
    }

    public record AniListCharacter(
            Long id,
            AniListName name,
            AniListImage image,
            String description
    ) {
    }

    public record AniListName(String first, String last, String full) {
    }

    public record AniListImage(String large) {
    }

    public record AniListError(String message) {
    }
}
