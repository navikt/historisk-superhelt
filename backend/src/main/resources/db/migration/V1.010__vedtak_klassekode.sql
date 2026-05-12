ALTER TABLE vedtak
    ADD COLUMN klassekode VARCHAR(100);

UPDATE vedtak v
SET klassekode = s.klassekode
FROM sak s
WHERE v.sak_id = s.sak_id
  AND v.klassekode IS NULL;