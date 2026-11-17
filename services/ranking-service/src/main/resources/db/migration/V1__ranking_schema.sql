CREATE TABLE processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ranked_votes (
    vote_id      UUID PRIMARY KEY,
    matchup_id   UUID         NOT NULL,
    user_id      VARCHAR(128) NOT NULL,
    winner_id    UUID         NOT NULL,
    loser_id     UUID         NOT NULL,
    difficulty   VARCHAR(10)  NOT NULL,
    vote_version BIGINT       NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_ranked_votes_order ON ranked_votes (occurred_at, vote_id);

CREATE TABLE ranked_characters (
    character_id UUID PRIMARY KEY,
    source       VARCHAR(16)  NOT NULL,
    external_id  BIGINT       NOT NULL,
    mal_id       BIGINT,
    name         VARCHAR(255) NOT NULL,
    image_url    TEXT,
    rating       DOUBLE PRECISION NOT NULL,
    wins         BIGINT       NOT NULL,
    losses       BIGINT       NOT NULL,
    votes        BIGINT       NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ranked_characters_rating ON ranked_characters (rating DESC);
