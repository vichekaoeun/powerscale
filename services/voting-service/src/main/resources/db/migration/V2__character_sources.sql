ALTER TABLE characters ADD COLUMN source VARCHAR(16);
ALTER TABLE characters ADD COLUMN external_id BIGINT;

UPDATE characters
SET source = 'JIKAN',
    external_id = mal_id;

ALTER TABLE characters ALTER COLUMN source SET NOT NULL;
ALTER TABLE characters ALTER COLUMN external_id SET NOT NULL;
ALTER TABLE characters ALTER COLUMN mal_id DROP NOT NULL;

ALTER TABLE characters DROP CONSTRAINT characters_mal_id_key;
ALTER TABLE characters ADD CONSTRAINT uq_characters_source_external UNIQUE (source, external_id);

CREATE UNIQUE INDEX uq_characters_mal_id ON characters (mal_id) WHERE mal_id IS NOT NULL;
