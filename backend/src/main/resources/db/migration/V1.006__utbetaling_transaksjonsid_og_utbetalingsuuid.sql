-- Gi utbetaling_uuid et nytt navn (transaksjons_id) og legg til utbetalings_uuid som stabil ID.

-- Steg 1: Legg til utbetalings_uuid-kolonne
ALTER TABLE utbetaling
    ADD COLUMN utbetalings_uuid UUID;

-- Steg 2: Kopier verdi fra eksisterende utbetaling_uuid
UPDATE utbetaling
SET utbetalings_uuid = utbetaling_uuid;

-- Steg 3: Sett NOT NULL
ALTER TABLE utbetaling
    ALTER COLUMN utbetalings_uuid SET NOT NULL;

-- Steg 4: Gi utbetaling_uuid nytt navn til transaksjons_id
ALTER TABLE utbetaling
    RENAME COLUMN utbetaling_uuid TO transaksjons_id;

-- Steg 5: Gi constraint og index nytt navn
ALTER INDEX IF EXISTS idx_utbetaling_uuid RENAME TO idx_utbetaling_transaksjons_id;
ALTER TABLE utbetaling
    RENAME CONSTRAINT uc_utbetaling_uuid TO uc_utbetaling_transaksjons_id;

-- Steg 6: Legg til index på utbetalings_uuid
CREATE INDEX idx_utbetaling_utbetalings_uuid ON utbetaling (utbetalings_uuid);
