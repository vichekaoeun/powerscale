package io.powerscale.ranking;

import io.powerscale.events.RankingFormula;
import io.powerscale.events.ScaleVoteSubmittedEvent;
import io.powerscale.ranking.scale.ScaleProcessedEventRepository;
import io.powerscale.ranking.scale.ScaleRankingConfigRepository;
import io.powerscale.ranking.scale.ScaleRankedItemRepository;
import io.powerscale.ranking.scale.ScaleRankedVoteRepository;
import io.powerscale.ranking.scale.ScaleVoteEventProcessor;
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
class ScaleRankingFlowIntegrationTest {

    private static final UUID SCALE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PEPPERONI = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID MUSHROOM = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScaleVoteEventProcessor processor;

    @Autowired
    private ScaleProcessedEventRepository processedEventRepository;

    @Autowired
    private ScaleRankedVoteRepository voteRepository;

    @Autowired
    private ScaleRankedItemRepository itemRepository;

    @Autowired
    private ScaleRankingConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        voteRepository.deleteAll();
        itemRepository.deleteAll();
        configRepository.deleteAll();
    }

    @Test
    void scaleLeaderboardIsBuiltIdempotently() throws Exception {
        ScaleVoteSubmittedEvent event = event(
                UUID.randomUUID(),
                UUID.randomUUID(),
                0L,
                PEPPERONI,
                MUSHROOM,
                "LOW"
        );

        processor.process(event);

        mockMvc.perform(get("/api/scale-leaderboard/{scaleId}", SCALE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.entries[0].name").value("Pepperoni"))
                .andExpect(jsonPath("$.entries[0].rating").value(1516.0))
                .andExpect(jsonPath("$.entries[1].name").value("Mushroom"))
                .andExpect(jsonPath("$.entries[1].rating").value(1484.0));

        processor.process(event);

        assertEquals(1L, voteRepository.count());
        assertEquals(1L, processedEventRepository.count());
    }

    @Test
    void changedScaleVoteReplacesThePreviousOutcome() throws Exception {
        UUID voteId = UUID.randomUUID();
        processor.process(event(UUID.randomUUID(), voteId, 0L, PEPPERONI, MUSHROOM, "LOW"));
        processor.process(event(UUID.randomUUID(), voteId, 1L, MUSHROOM, PEPPERONI, "HIGH"));

        mockMvc.perform(get("/api/scale-leaderboard/{scaleId}", SCALE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].name").value("Mushroom"))
                .andExpect(jsonPath("$.entries[0].rating").value(1508.0))
                .andExpect(jsonPath("$.entries[1].name").value("Pepperoni"))
                .andExpect(jsonPath("$.entries[1].rating").value(1492.0));
    }

    @Test
    void pointsFormulaAddsConfiguredPointsForTheWinner() throws Exception {
        RankingFormula points = new RankingFormula("POINTS", 1500.0, 32.0, 1.0, 0.75, 0.5, 5.0, 3.0, 1.0);
        ScaleVoteSubmittedEvent event = new ScaleVoteSubmittedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                SCALE_ID,
                "scale-user",
                snapshot(PEPPERONI),
                snapshot(MUSHROOM),
                "LOW",
                points,
                0L,
                Instant.now()
        );

        processor.process(event);

        mockMvc.perform(get("/api/scale-leaderboard/{scaleId}", SCALE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].name").value("Pepperoni"))
                .andExpect(jsonPath("$.entries[0].rating").value(5.0))
                .andExpect(jsonPath("$.entries[1].name").value("Mushroom"))
                .andExpect(jsonPath("$.entries[1].rating").value(0.0));
    }

    @Test
    void invalidLimitIsRejected() throws Exception {
        mockMvc.perform(get("/api/scale-leaderboard/{scaleId}", SCALE_ID).param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    private ScaleVoteSubmittedEvent event(
            UUID eventId,
            UUID voteId,
            long version,
            UUID winnerId,
            UUID loserId,
            String difficulty
    ) {
        return new ScaleVoteSubmittedEvent(
                eventId,
                voteId,
                SCALE_ID,
                "scale-user",
                snapshot(winnerId),
                snapshot(loserId),
                difficulty,
                version,
                Instant.now()
        );
    }

    private ScaleVoteSubmittedEvent.ScaleItemSnapshot snapshot(UUID id) {
        if (id.equals(PEPPERONI)) {
            return new ScaleVoteSubmittedEvent.ScaleItemSnapshot(id, "Pepperoni", null);
        }
        return new ScaleVoteSubmittedEvent.ScaleItemSnapshot(id, "Mushroom", "mushroom.jpg");
    }
}
