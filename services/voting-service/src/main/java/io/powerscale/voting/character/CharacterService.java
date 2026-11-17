package io.powerscale.voting.character;

import io.powerscale.voting.client.AniListClient;
import io.powerscale.voting.client.ExternalCharacter;
import io.powerscale.voting.web.BadRequestException;
import io.powerscale.voting.web.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CharacterService {

    private static final int ROSTER_SIZE = 500;
    private static final int MAX_ROSTER_LIMIT = 500;
    private static final int ROSTER_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CharacterRepository characterRepository;
    private final AniListClient aniListClient;

    public CharacterService(CharacterRepository characterRepository, AniListClient aniListClient) {
        this.characterRepository = characterRepository;
        this.aniListClient = aniListClient;
    }

    @Transactional(readOnly = true)
    public PageResponse<CharacterResponse> list(String search, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Character> characters = (search == null || search.isBlank())
                ? characterRepository.findAll(pageRequest)
                : characterRepository.findByNameContainingIgnoreCase(search.trim(), pageRequest);

        return PageResponse.from(characters, CharacterResponse::from);
    }

    @Transactional(readOnly = true)
    public CharacterResponse get(UUID id) {
        return CharacterResponse.from(findCharacter(id));
    }

    @Transactional
    public List<CharacterResponse> addManualCharacters(CreateCharactersRequest request) {
        List<Character> toSave = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (CreateCharacterRequest item : request.characters()) {
            String name = item.name().trim();
            String key = name.toLowerCase(Locale.ROOT);
            if (!seen.add(key) || characterRepository.existsByNameIgnoreCase(name)) {
                continue;
            }

            toSave.add(new Character(
                    CharacterSource.CUSTOM,
                    newExternalId(),
                    null,
                    name,
                    normalizeImageUrl(item.imageUrl()),
                    null,
                    null
            ));
        }

        return characterRepository.saveAll(toSave).stream()
                .map(CharacterResponse::from)
                .toList();
    }

    private long newExternalId() {
        long candidate;
        do {
            candidate = Math.abs(SECURE_RANDOM.nextLong());
        } while (candidate == 0
                || characterRepository.existsBySourceAndExternalId(CharacterSource.CUSTOM, candidate));
        return candidate;
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://")
                || trimmed.startsWith("https://")
                || trimmed.startsWith("/api/uploads/")) {
            return trimmed;
        }
        return null;
    }

    @Transactional
    public List<CharacterResponse> importRoster(int limit, int offset) {
        if (offset < 0) {
            throw new BadRequestException("offset must be zero or greater");
        }
        if (limit < 1 || limit > MAX_ROSTER_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_ROSTER_LIMIT);
        }
        if (offset >= ROSTER_SIZE) {
            return List.of();
        }

        int effectiveLimit = Math.min(limit, ROSTER_SIZE - offset);
        int startPage = (offset / ROSTER_PAGE_SIZE) + 1;
        int skip = offset % ROSTER_PAGE_SIZE;
        int page = startPage;
        List<ExternalCharacter> collected = new ArrayList<>();

        while (collected.size() < effectiveLimit) {
            AniListClient.RosterPage result = aniListClient.fetchRoster(page, ROSTER_PAGE_SIZE);
            List<ExternalCharacter> pageItems = result.characters();
            if (page == startPage && skip > 0) {
                pageItems = pageItems.subList(Math.min(skip, pageItems.size()), pageItems.size());
            }
            if (pageItems.isEmpty()) {
                break;
            }
            collected.addAll(pageItems);
            if (!result.hasNextPage()) {
                break;
            }
            page++;
        }

        Map<Long, ExternalCharacter> unique = new LinkedHashMap<>();
        for (ExternalCharacter character : collected) {
            unique.putIfAbsent(character.externalId(), character);
        }

        return persist(unique.values().stream().limit(effectiveLimit).toList());
    }

    private List<CharacterResponse> persist(List<ExternalCharacter> externalCharacters) {
        List<Character> imported = new ArrayList<>();
        for (ExternalCharacter external : externalCharacters) {
            Character character = characterRepository
                    .findBySourceAndExternalId(external.source(), external.externalId())
                    .orElse(null);
            if (character == null) {
                character = new Character(
                        external.source(),
                        external.externalId(),
                        external.malId(),
                        external.name(),
                        external.imageUrl(),
                        external.about(),
                        external.favorites()
                );
            } else {
                character.updateFrom(
                        external.source(),
                        external.externalId(),
                        external.malId(),
                        external.name(),
                        external.imageUrl(),
                        external.about(),
                        external.favorites()
                );
            }
            imported.add(character);
        }

        return characterRepository.saveAll(imported).stream()
                .map(CharacterResponse::from)
                .toList();
    }

    Character findCharacter(UUID id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character %s not found".formatted(id)));
    }
}
