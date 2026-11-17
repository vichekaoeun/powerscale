ALTER TABLE scales ADD COLUMN selection1 VARCHAR(40);
ALTER TABLE scales ADD COLUMN selection2 VARCHAR(40);
ALTER TABLE scales ADD COLUMN selection3 VARCHAR(40);

UPDATE scales
SET selection1 = 'Low',
    selection2 = 'Mid',
    selection3 = 'High';
