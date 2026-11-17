package io.powerscale.voting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.voting.character.Character;
import io.powerscale.voting.character.CharacterRepository;
import io.powerscale.voting.character.CharacterSource;
import io.powerscale.voting.matchup.MatchupRepository;
import io.powerscale.voting.outbox.OutboxEventRepository;
import io.powerscale.voting.vote.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VotingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private MatchupRepository matchupRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        voteRepository.deleteAll();
        matchupRepository.deleteAll();
        characterRepository.deleteAll();
    }

    @Test
    void userCanVoteAndChangeTheirVoteWithoutCreatingDuplicates() throws Exception {
        characterRepository.save(character(1L, "Alpha", "alpha.jpg", "First", 100));
        characterRepository.save(character(2L, "Beta", "beta.jpg", "Second", 90));

        JsonNode matchup = fetchRandomMatchup();
        String matchupId = matchup.get("id").asText();
        String winnerId = matchup.get("characterA").get("id").asText();

        submitVote(matchupId, winnerId, "LOW")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winner.id").value(winnerId))
                .andExpect(jsonPath("$.difficulty").value("LOW"));

        assertEquals(1, voteRepository.count());
        assertEquals(1, outboxEventRepository.count());
        assertTrue(hasOutboxDifficulty("LOW"));

        submitVote(matchupId, winnerId, "HIGH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("HIGH"));

        assertEquals(1, voteRepository.count());
        assertEquals(2, outboxEventRepository.count());
        assertTrue(hasOutboxDifficulty("HIGH"));

        mockMvc.perform(get("/api/matchups/{id}/stats", matchupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));

        mockMvc.perform(get("/api/votes/me/{matchupId}", matchupId).header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("HIGH"));

        mockMvc.perform(get("/api/votes/me").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void votingRequiresAUserHeader() throws Exception {
        characterRepository.save(character(1L, "Alpha", null, null, 0));
        characterRepository.save(character(2L, "Beta", null, null, 0));

        JsonNode matchup = fetchRandomMatchup();
        String body = objectMapper.writeValueAsString(Map.of(
                "matchupId", matchup.get("id").asText(),
                "winnerId", matchup.get("characterA").get("id").asText(),
                "difficulty", "MID"
        ));

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void winnerMustBelongToTheMatchup() throws Exception {
        characterRepository.save(character(1L, "Alpha", null, null, 0));
        characterRepository.save(character(2L, "Beta", null, null, 0));

        JsonNode matchup = fetchRandomMatchup();
        Character outsider = characterRepository.save(character(3L, "Sanji", null, null, 0));

        String body = objectMapper.writeValueAsString(Map.of(
                "matchupId", matchup.get("id").asText(),
                "winnerId", outsider.getId().toString(),
                "difficulty", "MID"
        ));

        mockMvc.perform(post("/api/votes")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void matchupGenerationRequiresTwoCharacters() throws Exception {
        characterRepository.save(character(1L, "Alpha", null, null, 0));

        mockMvc.perform(get("/api/matchups/random"))
                .andExpect(status().isConflict());
    }

    private Character character(long externalId, String name, String imageUrl, String about, Integer favorites) {
        return new Character(CharacterSource.ANILIST, externalId, null, name, imageUrl, about, favorites);
    }

    private boolean hasOutboxDifficulty(String difficulty) throws Exception {
        for (var event : outboxEventRepository.findAll()) {
            JsonNode payload = objectMapper.readTree(event.getPayload());
            if (difficulty.equals(payload.get("difficulty").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode fetchRandomMatchup() throws Exception {
        String response = mockMvc.perform(get("/api/matchups/random"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private org.springframework.test.web.servlet.ResultActions submitVote(
            String matchupId,
            String winnerId,
            String difficulty
    ) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "matchupId", matchupId,
                "winnerId", winnerId,
                "difficulty", difficulty
        ));

        return mockMvc.perform(post("/api/votes")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
