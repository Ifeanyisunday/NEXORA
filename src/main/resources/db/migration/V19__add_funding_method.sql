ALTER TABLE fundings
ADD COLUMN method VARCHAR(30);

UPDATE fundings
SET method = 'CARD'
WHERE method IS NULL;

ALTER TABLE fundings
ALTER COLUMN method SET NOT NULL;