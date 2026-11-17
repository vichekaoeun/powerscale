package io.powerscale.voting.matchup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/matchups")
public class MatchupController {

    private final MatchupService matchupService;

    public MatchupController(MatchupService matchupService) {
        this.matchupService = matchupService;
    }

    @GetMapping("/random")
    public MatchupResponse random() {
        return matchupService.getOrCreateRandomMatchup();
    }

    @GetMapping("/{id}")
    public MatchupResponse get(@PathVariable UUID id) {
        return matchupService.get(id);
    }

    @GetMapping("/{id}/stats")
    public MatchupStatsResponse stats(@PathVariable UUID id) {
        return matchupService.stats(id);
    }
}
