package io.powerscale.voting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.voting.outbox.OutboxEventRepository;
import io.powerscale.voting.scale.ScaleItemRepository;
import io.powerscale.voting.scale.ScaleMatchupRepository;
import io.powerscale.voting.scale.ScaleRepository;
import io.powerscale.voting.scale.ScaleVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScaleFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScaleRepository scaleRepository;

    @Autowired
    private ScaleItemRepository itemRepository;

    @Autowired
    private ScaleMatchupRepository matchupRepository;

    @Autowired
    private ScaleVoteRepository voteRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        voteRepository.deleteAll();
        matchupRepository.deleteAll();
        itemRepository.deleteAll();
        scaleRepository.deleteAll();
    }

    @Test
    void createImportAndVoteOnACustomScale() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "Best Pizza Toppings",
                "criterion", "Taste",
                "description", "Settle the topping debate.",
                "items", List.of(
                        Map.of("name", "Pepperoni"),
                        Map.of("name", "Mushroom", "imageUrl", "https://cdn.example/mushroom.jpg"),
                        Map.of("name", "Pineapple")
                ),
                "selections", List.of("Low", "Mid", "High")
        ));

        JsonNode created = read(mockMvc.perform(post("/api/scales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scale.title").value("Best Pizza Toppings"))
                .andExpect(jsonPath("$.scale.items", hasSize(3)))
                .andExpect(jsonPath("$.creatorKey").isNotEmpty())
                .andReturn().getResponse().getContentAsString());

        String slug = created.get("scale").get("slug").asText();

        JsonNode matchup = read(mockMvc.perform(get("/api/scales/{slug}/matchups/random", slug))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        String matchupId = matchup.get("id").asText();
        String winnerId = matchup.get("itemA").get("id").asText();

        mockMvc.perform(post("/api/scales/{slug}/votes", slug)
                        .header("X-User-Id", "scale-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "matchupId", matchupId,
                                "winnerId", winnerId,
                                "difficulty", "LOW"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winner.id").value(winnerId));

        assertEquals(1, voteRepository.count());
        assertEquals(1, outboxEventRepository.count());

        String otherItemId = matchup.get("itemB").get("id").asText();
        mockMvc.perform(post("/api/scales/{slug}/votes", slug)
                        .header("X-User-Id", "scale-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "matchupId", matchupId,
                                "winnerId", otherItemId,
                                "difficulty", "HIGH"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winner.id").value(otherItemId));

        assertEquals(1, voteRepository.count());
        assertEquals(2, outboxEventRepository.count());

        mockMvc.perform(get("/api/scales/{slug}/matchups/{matchupId}/stats", slug, matchupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));

        mockMvc.perform(post("/api/scales/{slug}/items", slug)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items", List.of(Map.of("name", "Olives"))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(4));

        mockMvc.perform(get("/api/scales").param("search", "pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].slug").value(slug));

        mockMvc.perform(get("/api/scales").param("search", "nothing-matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void creatingAScaleRequiresAtLeastTwoItems() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "Too Small",
                "criterion", "Anything",
                "items", List.of(Map.of("name", "Only One"))
        ));

        mockMvc.perform(post("/api/scales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private JsonNode read(String value) throws Exception {
        return objectMapper.readTree(value);
    }
}
