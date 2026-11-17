package io.powerscale.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/voting")
    public ResponseEntity<Map<String, String>> votingFallback() {
        return unavailable("voting-service");
    }

    @RequestMapping("/fallback/ranking")
    public ResponseEntity<Map<String, String>> rankingFallback() {
        return unavailable("ranking-service");
    }

    private ResponseEntity<Map<String, String>> unavailable(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "unavailable",
                "service", service
        ));
    }
}
