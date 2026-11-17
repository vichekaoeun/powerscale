package io.powerscale.voting.client;

import com.sun.net.httpserver.HttpServer;
import io.powerscale.voting.character.CharacterSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AniListClientTest {

    private HttpServer server;
    private AniListClient aniListClient;
    private volatile String responseBody;

    @BeforeEach
    void setUp() throws Exception {
        responseBody = """
                {
                  "data": {
                    "Media": {
                      "characters": {
                        "pageInfo": { "hasNextPage": false, "total": 2 },
                        "nodes": [
                          {
                            "id": 40,
                            "name": { "first": "Aria", "last": "Vale", "full": "Aria Vale" },
                            "image": { "large": "https://cdn.example/luffy.jpg" },
                            "description": "<p>A test character</p>"
                          },
                          {
                            "id": 64,
                            "name": { "first": "Franky", "last": "", "full": "Franky" },
                            "image": null,
                            "description": null
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + server.getAddress().getPort())
                .build();
        aniListClient = new AniListClient(restClient);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void mapsRosterWithWesternNameOrder() {
        AniListClient.RosterPage page = aniListClient.fetchRoster(1, 25);

        assertEquals(2, page.characters().size());
        assertEquals(2, page.total());
        assertTrue(!page.hasNextPage());

        ExternalCharacter luffy = page.characters().getFirst();
        assertEquals(CharacterSource.ANILIST, luffy.source());
        assertEquals(40L, luffy.externalId());
        assertEquals("Vale Aria", luffy.name());
        assertEquals("https://cdn.example/luffy.jpg", luffy.imageUrl());
        assertEquals("A test character", luffy.about());

        assertEquals("Franky", page.characters().get(1).name());
    }

    @Test
    void surfacesGraphQlErrors() {
        responseBody = "{\"errors\":[{\"message\":\"rate limited\"}]}";

        assertThrows(
                AniListUnavailableException.class,
                () -> aniListClient.fetchRoster(1, 25)
        );
    }
}
