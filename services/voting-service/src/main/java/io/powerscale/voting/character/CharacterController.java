package io.powerscale.voting.character;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping
    public PageResponse<CharacterResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return characterService.list(search, page, size);
    }

    @GetMapping("/{id}")
    public CharacterResponse get(@PathVariable UUID id) {
        return characterService.get(id);
    }

    @PostMapping("/manual")
    public List<CharacterResponse> addManualCharacters(
            @Valid @RequestBody CreateCharactersRequest request
    ) {
        return characterService.addManualCharacters(request);
    }

    @PostMapping("/import-roster")
    public List<CharacterResponse> importRoster(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return characterService.importRoster(limit, offset);
    }
}
