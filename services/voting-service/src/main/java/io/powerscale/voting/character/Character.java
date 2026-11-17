package io.powerscale.voting.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "characters",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_characters_source_external",
                columnNames = {"source", "external_id"}
        )
)
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CharacterSource source;

    @Column(name = "external_id", nullable = false)
    private long externalId;

    @Column(name = "mal_id")
    private Long malId;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(columnDefinition = "text")
    private String about;

    private Integer favorites;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Character() {
    }

    public Character(
            CharacterSource source,
            long externalId,
            Long malId,
            String name,
            String imageUrl,
            String about,
            Integer favorites
    ) {
        this.source = source;
        this.externalId = externalId;
        this.malId = malId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.about = about;
        this.favorites = favorites;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void updateFrom(
            CharacterSource source,
            long externalId,
            Long malId,
            String name,
            String imageUrl,
            String about,
            Integer favorites
    ) {
        this.source = source;
        this.externalId = externalId;
        this.malId = malId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.about = about;
        this.favorites = favorites;
    }

    public UUID getId() {
        return id;
    }

    public CharacterSource getSource() {
        return source;
    }

    public long getExternalId() {
        return externalId;
    }

    public Long getMalId() {
        return malId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAbout() {
        return about;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
