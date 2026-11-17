package io.powerscale.voting.matchup;

import io.powerscale.voting.character.Character;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "matchups",
        uniqueConstraints = @UniqueConstraint(name = "uq_matchups_pair", columnNames = {"character_a_id", "character_b_id"})
)
public class Matchup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_a_id", nullable = false)
    private Character characterA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_b_id", nullable = false)
    private Character characterB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Matchup() {
    }

    public Matchup(Character characterA, Character characterB) {
        this.characterA = characterA;
        this.characterB = characterB;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Character getCharacterA() {
        return characterA;
    }

    public Character getCharacterB() {
        return characterB;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean includes(UUID characterId) {
        return characterA.getId().equals(characterId) || characterB.getId().equals(characterId);
    }
}
