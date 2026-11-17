package io.powerscale.ranking;

import io.powerscale.events.VoteSubmittedEvent;
import io.powerscale.ranking.character.RankedCharacterRepository;
import io.powerscale.ranking.event.ProcessedEventRepository;
import io.powerscale.ranking.event.RankedVoteRepository;
import io.powerscale.ranking.event.VoteEventProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RankingFlowIntegrationTest {

    private static final UUID LUFFY = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ZORO = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VoteEventProcessor processor;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private RankedVoteRepository voteRepository;

    @Autowired
    private RankedCharacterRepository characterRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        voteRepository.deleteAll();
        characterRepository.deleteAll();
    }

    @Test
    void processedEventUpdatesTheLeaderboardAndIsIdempotent() throws Exception {
        VoteSubmittedEvent event = event(UUID.randomUUID(), UUID.randomUUID(), 0L, LUFFY, ZORO, "LOW");

        processor.process(event);

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.entries[0].name").value("Alpha"))
                .andExpect(jsonPath("$.entries[0].rating").value(1516.0))
                .andExpect(jsonPath("$.entries[1].name").value("Beta"))
                .andExpect(jsonPath("$.entries[1].rating").value(1484.0));

        processor.process(event);

        assertEquals(1L, voteRepository.count());
        assertEquals(1L, processedEventRepository.count());
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.entries[0].rating").value(1516.0));
    }

    @Test
    void changedVoteIsAppliedAndOlderEventsAreIgnored() throws Exception {
        UUID voteId = UUID.randomUUID();
        processor.process(event(UUID.randomUUID(), voteId, 0L, LUFFY, ZORO, "LOW"));
        processor.process(event(UUID.randomUUID(), voteId, 1L, ZORO, LUFFY, "HIGH"));

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.entries[0].name").value("Beta"))
                .andExpect(jsonPath("$.entries[0].rating").value(1508.0))
                .andExpect(jsonPath("$.entries[1].name").value("Alpha"))
                .andExpect(jsonPath("$.entries[1].rating").value(1492.0));

        processor.process(event(UUID.randomUUID(), voteId, 0L, LUFFY, ZORO, "LOW"));

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(jsonPath("$.entries[0].name").value("Beta"))
                .andExpect(jsonPath("$.entries[0].rating").value(1508.0));
    }

    @Test
    void leaderboardRejectsInvalidLimits() throws Exception {
        mockMvc.perform(get("/api/leaderboard").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    private VoteSubmittedEvent event(
            UUID eventId,
            UUID voteId,
            long version,
            UUID winnerId,
            UUID loserId,
            String difficulty
    ) {
        return new VoteSubmittedEvent(
                eventId,
                voteId,
                UUID.randomUUID(),
                "user-1",
                snapshot(winnerId),
                snapshot(loserId),
                difficulty,
                version,
                Instant.now()
        );
    }

    private VoteSubmittedEvent.CharacterSnapshot snapshot(UUID id) {
        if (id.equals(LUFFY)) {
            return new VoteSubmittedEvent.CharacterSnapshot(
                    LUFFY,
                    "ANILIST",
                    40L,
                    null,
                    "Alpha",
                    "luffy.jpg"
            );
        }
        return new VoteSubmittedEvent.CharacterSnapshot(
                ZORO,
                "ANILIST",
                62L,
                null,
                "Beta",
                "zoro.jpg"
        );
    }
}
