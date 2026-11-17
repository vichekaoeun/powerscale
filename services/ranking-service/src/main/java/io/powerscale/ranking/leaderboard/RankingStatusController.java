package io.powerscale.ranking.leaderboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rankings")
public class RankingStatusController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "service", "ranking-service",
                "status", "UP",
                "consumer", "vote-submitted"
        );
    }
}
