package io.powerscale.ranking.scale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/scale-leaderboard")
public class ScaleLeaderboardController {

    private final ScaleLeaderboardService leaderboardService;

    public ScaleLeaderboardController(ScaleLeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/{scaleId}")
    public ScaleLeaderboardResponse leaderboard(
            @PathVariable UUID scaleId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return leaderboardService.getLeaderboard(scaleId, limit);
    }
}
