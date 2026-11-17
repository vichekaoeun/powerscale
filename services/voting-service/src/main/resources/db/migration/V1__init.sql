CREATE TABLE characters (
    id          UUID PRIMARY KEY,
    mal_id      BIGINT       NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    image_url   TEXT,
    about       TEXT,
    favorites   INTEGER,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE matchups (
    id             UUID PRIMARY KEY,
    character_a_id UUID        NOT NULL REFERENCES characters (id),
    character_b_id UUID        NOT NULL REFERENCES characters (id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_matchups_pair UNIQUE (character_a_id, character_b_id),
    CONSTRAINT chk_matchups_distinct CHECK (character_a_id <> character_b_id)
);

CREATE TABLE votes (
    id         UUID PRIMARY KEY,
    user_id    VARCHAR(128) NOT NULL,
    matchup_id UUID         NOT NULL REFERENCES matchups (id),
    winner_id  UUID         NOT NULL REFERENCES characters (id),
    difficulty VARCHAR(10)  NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_votes_user_matchup UNIQUE (user_id, matchup_id)
);

CREATE INDEX idx_votes_matchup ON votes (matchup_id);
CREATE INDEX idx_votes_user ON votes (user_id);
CREATE INDEX idx_votes_winner ON votes (winner_id);
