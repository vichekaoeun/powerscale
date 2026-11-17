package io.powerscale.voting.scale;

import io.powerscale.voting.character.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/scales")
public class ScaleController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final ScaleService scaleService;

    public ScaleController(ScaleService scaleService) {
        this.scaleService = scaleService;
    }

    @PostMapping
    public CreateScaleResponse create(@Valid @RequestBody CreateScaleRequest request) {
        return scaleService.createScale(request);
    }

    @GetMapping
    public PageResponse<ScaleSummaryResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        return scaleService.listScales(search, page, size);
    }

    @GetMapping("/{slug}")
    public ScaleResponse get(@PathVariable String slug) {
        return scaleService.getScale(slug);
    }

    @PostMapping("/{slug}/items")
    public ScaleResponse addItems(
            @PathVariable String slug,
            @Valid @RequestBody AddScaleItemsRequest request
    ) {
        return scaleService.addItems(slug, request);
    }

    @GetMapping("/{slug}/matchups/random")
    public ScaleMatchupResponse randomMatchup(@PathVariable String slug) {
        return scaleService.getOrCreateRandomMatchup(slug);
    }

    @GetMapping("/{slug}/matchups/{matchupId}/stats")
    public ScaleMatchupStatsResponse stats(
            @PathVariable String slug,
            @PathVariable UUID matchupId
    ) {
        return scaleService.stats(slug, matchupId);
    }

    @PostMapping("/{slug}/votes")
    public ScaleVoteResponse vote(
            @PathVariable String slug,
            @RequestHeader(USER_ID_HEADER) String userId,
            @Valid @RequestBody ScaleVoteRequest request
    ) {
        return scaleService.submitVote(slug, userId, request);
    }

    @GetMapping("/{slug}/votes/me/{matchupId}")
    public ResponseEntity<ScaleVoteResponse> myVote(
            @PathVariable String slug,
            @PathVariable UUID matchupId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return scaleService.getUserVote(slug, userId, matchupId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
