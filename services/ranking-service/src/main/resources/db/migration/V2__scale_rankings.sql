CREATE TABLE scale_processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE scale_ranked_votes (
    vote_id      UUID PRIMARY KEY,
    scale_id     UUID         NOT NULL,
    user_id      VARCHAR(128) NOT NULL,
    winner_id    UUID         NOT NULL,
    loser_id     UUID         NOT NULL,
    difficulty   VARCHAR(10)  NOT NULL,
    vote_version BIGINT       NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_scale_ranked_votes_scale ON scale_ranked_votes (scale_id, occurred_at, vote_id);

CREATE TABLE scale_ranked_items (
    item_id    UUID PRIMARY KEY,
    scale_id   UUID         NOT NULL,
    name       VARCHAR(160) NOT NULL,
    image_url  TEXT,
    rating     DOUBLE PRECISION NOT NULL,
    wins       BIGINT       NOT NULL,
    losses     BIGINT       NOT NULL,
    votes      BIGINT       NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_scale_ranked_items_scale ON scale_ranked_items (scale_id, rating DESC);
