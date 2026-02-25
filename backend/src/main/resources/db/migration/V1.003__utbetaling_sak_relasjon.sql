-- Flytt relasjonen mellom sak og utbetaling fra 1:1 til 1:mange.
-- Utbetaling får en sak_id FK i stedet for at sak peker på utbetaling.

-- Steg 1: Legg til sak_id på utbetaling (nullable midlertidig)
ALTER TABLE utbetaling
    ADD COLUMN sak_id BIGINT;

-- Steg 2: Populer sak_id fra eksisterende sak.utbetaling_id
UPDATE utbetaling u
SET sak_id = s.sak_id
FROM sak s
WHERE s.utbetaling_id = u.utbetaling_id;

-- Steg 2b: Fjern utbetalinger som ikke er tilknyttet noen sak
DELETE FROM utbetaling WHERE sak_id IS NULL;

-- Steg 3: Sett NOT NULL og legg til FK-constraint
ALTER TABLE utbetaling
    ALTER COLUMN sak_id SET NOT NULL;

ALTER TABLE utbetaling
    ADD CONSTRAINT FK_UTBETALING_ON_SAK FOREIGN KEY (sak_id) REFERENCES sak (sak_id);

CREATE INDEX idx_utbetaling_sak_id ON utbetaling (sak_id);

-- Steg 4: Fjern utbetaling_id fra sak
ALTER TABLE sak
    DROP CONSTRAINT IF EXISTS FK_SAK_ON_UTBETALING;

ALTER TABLE sak
    DROP COLUMN utbetaling_id;
