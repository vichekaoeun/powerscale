CREATE TABLE scales (
    id               UUID PRIMARY KEY,
    slug             VARCHAR(80)  NOT NULL UNIQUE,
    title            VARCHAR(120) NOT NULL,
    criterion        VARCHAR(120) NOT NULL,
    description      TEXT,
    creator_key_hash VARCHAR(64)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE scale_items (
    id         UUID PRIMARY KEY,
    scale_id   UUID         NOT NULL REFERENCES scales (id) ON DELETE CASCADE,
    name       VARCHAR(160) NOT NULL,
    image_url  TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_scale_items_scale ON scale_items (scale_id);

CREATE TABLE scale_matchups (
    id         UUID PRIMARY KEY,
    scale_id   UUID        NOT NULL REFERENCES scales (id) ON DELETE CASCADE,
    item_a_id  UUID        NOT NULL REFERENCES scale_items (id),
    item_b_id  UUID        NOT NULL REFERENCES scale_items (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_scale_matchups_pair UNIQUE (scale_id, item_a_id, item_b_id),
    CONSTRAINT chk_scale_matchups_distinct CHECK (item_a_id <> item_b_id)
);

CREATE INDEX idx_scale_matchups_scale ON scale_matchups (scale_id);

CREATE TABLE scale_votes (
    id         UUID PRIMARY KEY,
    scale_id   UUID         NOT NULL REFERENCES scales (id) ON DELETE CASCADE,
    matchup_id UUID         NOT NULL REFERENCES scale_matchups (id),
    user_id    VARCHAR(128) NOT NULL,
    winner_id  UUID         NOT NULL REFERENCES scale_items (id),
    difficulty VARCHAR(10)  NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_scale_votes_user_matchup UNIQUE (user_id, matchup_id)
);

CREATE INDEX idx_scale_votes_scale ON scale_votes (scale_id);
CREATE INDEX idx_scale_votes_matchup ON scale_votes (matchup_id);
