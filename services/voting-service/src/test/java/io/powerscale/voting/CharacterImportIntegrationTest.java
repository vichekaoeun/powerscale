package io.powerscale.voting;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.powerscale.voting.character.CharacterRepository;
import io.powerscale.voting.character.CharacterSource;
import io.powerscale.voting.client.AniListClient;
import io.powerscale.voting.client.ExternalCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CharacterRepository characterRepository;

    @MockitoBean
    private AniListClient aniListClient;

    @BeforeEach
    void setUp() {
        characterRepository.deleteAll();
    }

    @Test
    void importingTheSameAniListCharacterUpdatesInsteadOfDuplicating() throws Exception {
        when(aniListClient.fetchRoster(1, 25)).thenReturn(new AniListClient.RosterPage(
                List.of(new ExternalCharacter(
                        CharacterSource.ANILIST,
                        40L,
                        null,
                        "Aria Vale",
                        "luffy.jpg",
                        "Captain",
                        null
                )),
                false,
                1
        ));

        mockMvc.perform(post("/api/characters/import-roster").param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Aria Vale"));

        when(aniListClient.fetchRoster(1, 25)).thenReturn(new AniListClient.RosterPage(
                List.of(new ExternalCharacter(
                        CharacterSource.ANILIST,
                        40L,
                        null,
                        "Aria",
                        "luffy-new.jpg",
                        "Test character",
                        null
                )),
                false,
                1
        ));

        mockMvc.perform(post("/api/characters/import-roster").param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aria"));

        assertEquals(1, characterRepository.count());
        assertEquals(
                "Test character",
                characterRepository.findBySourceAndExternalId(CharacterSource.ANILIST, 40L)
                        .orElseThrow()
                        .getAbout()
        );
    }

    @Test
    void addsManualCharactersAndSkipsDuplicates() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "characters", List.of(
                        Map.of("name", "Kaidou", "imageUrl", "/api/uploads/kaidou.png"),
                        Map.of("name", "kaidou"),
                        Map.of("name", "Yamato")
                )
        ));

        mockMvc.perform(post("/api/characters/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        assertEquals(2, characterRepository.count());
        assertEquals(
                "/api/uploads/kaidou.png",
                characterRepository.findByNameIgnoreCase("Kaidou").orElseThrow().getImageUrl()
        );

        mockMvc.perform(post("/api/characters/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rosterImportRejectsInvalidLimits() throws Exception {
        mockMvc.perform(post("/api/characters/import-roster").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }
}
