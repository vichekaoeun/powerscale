package io.powerscale.voting.client;

import io.powerscale.voting.character.CharacterSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

@Component
public class AniListClient {

    private static final String ROSTER_QUERY = """
            query ($page: Int, $perPage: Int) {
              Media(id: 21, type: ANIME) {
                characters(page: $page, perPage: $perPage, sort: [ROLE, RELEVANCE, ID]) {
                  pageInfo { hasNextPage total }
                  nodes {
                    id
                    name { first last full }
                    image { large }
                    description
                  }
                }
              }
            }
            """;

    private final RestClient restClient;

    public AniListClient(@Qualifier("aniListRestClient") RestClient aniListRestClient) {
        this.restClient = aniListRestClient;
    }

    @Retryable(
            retryFor = AniListUnavailableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public RosterPage fetchRoster(int page, int perPage) {
        try {
            AniListRosterResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "query", ROSTER_QUERY,
                            "variables", Map.of("page", page, "perPage", perPage)
                    ))
                    .retrieve()
                    .body(AniListRosterResponse.class);

            if (response == null) {
                throw new AniListUnavailableException("AniList returned an empty response");
            }
            if (response.errors() != null && !response.errors().isEmpty()) {
                String message = response.errors().stream()
                        .map(AniListRosterResponse.AniListError::message)
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst()
                        .orElse("unknown error");
                throw new AniListUnavailableException("AniList request failed: " + message);
            }
            if (response.data() == null
                    || response.data().media() == null
                    || response.data().media().characters() == null) {
                throw new AniListUnavailableException("AniList returned no roster");
            }

            AniListRosterResponse.AniListCharacters characters = response.data().media().characters();
            List<ExternalCharacter> roster = characters.nodes().stream()
                    .filter(node -> node != null && node.id() != null && !displayName(node.name()).isBlank())
                    .map(node -> new ExternalCharacter(
                            CharacterSource.ANILIST,
                            node.id(),
                            null,
                            displayName(node.name()),
                            node.image() != null ? node.image().large() : null,
                            cleanDescription(node.description()),
                            null
                    ))
                    .toList();

            return new RosterPage(
                    roster,
                    characters.pageInfo() != null && characters.pageInfo().hasNextPage(),
                    characters.pageInfo() != null ? characters.pageInfo().total() : roster.size()
            );
        } catch (RestClientException exception) {
            throw new AniListUnavailableException("AniList API request failed", exception);
        }
    }

    private String displayName(AniListRosterResponse.AniListName name) {
        if (name == null) {
            return "";
        }

        String first = trimToNull(name.first());
        String last = trimToNull(name.last());
        if (first != null && last != null) {
            return last + " " + first;
        }
        if (first != null) {
            return first;
        }
        if (last != null) {
            return last;
        }
        return trimToNull(name.full()) != null ? name.full().trim() : "";
    }

    private String cleanDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String withoutTags = description.replaceAll("<[^>]+>", " ");
        return HtmlUtils.htmlUnescape(withoutTags).replaceAll("\\s+", " ").trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record RosterPage(
            List<ExternalCharacter> characters,
            boolean hasNextPage,
            int total
    ) {
    }
}
