CREATE TABLE scale_ranking_configs (
    scale_id    UUID PRIMARY KEY,
    method      VARCHAR(20) NOT NULL,
    base_rating DOUBLE PRECISION NOT NULL,
    k_factor    DOUBLE PRECISION NOT NULL,
    low_weight  DOUBLE PRECISION NOT NULL,
    mid_weight  DOUBLE PRECISION NOT NULL,
    high_weight DOUBLE PRECISION NOT NULL,
    low_points  DOUBLE PRECISION NOT NULL,
    mid_points  DOUBLE PRECISION NOT NULL,
    high_points DOUBLE PRECISION NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
