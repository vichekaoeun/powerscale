package io.powerscale.ranking.leaderboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class LeaderboardCache {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardCache.class);
    private static final String EPOCH_KEY = "leaderboard:epoch";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final long ttlSeconds;

    public LeaderboardCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.leaderboard-enabled:true}") boolean enabled,
            @Value("${app.cache.leaderboard-ttl-seconds:30}") long ttlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<LeaderboardResponse> get(int limit) {
        if (!enabled) {
            return Optional.empty();
        }

        try {
            String payload = redisTemplate.opsForValue().get(cacheKey(limit));
            if (payload == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(payload, LeaderboardResponse.class));
        } catch (Exception exception) {
            log.warn("Could not read leaderboard from Redis", exception);
            return Optional.empty();
        }
    }

    public void put(int limit, LeaderboardResponse response) {
        if (!enabled) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey(limit), payload, Duration.ofSeconds(ttlSeconds));
        } catch (Exception exception) {
            log.warn("Could not write leaderboard to Redis", exception);
        }
    }

    public void invalidate() {
        if (!enabled) {
            return;
        }

        try {
            redisTemplate.opsForValue().increment(EPOCH_KEY);
        } catch (Exception exception) {
            log.warn("Could not invalidate leaderboard cache", exception);
        }
    }

    private String cacheKey(int limit) {
        String epoch = redisTemplate.opsForValue().get(EPOCH_KEY);
        return "leaderboard:" + (epoch == null ? "0" : epoch) + ":" + limit;
    }
}
