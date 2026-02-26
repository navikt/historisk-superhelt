-- Flytt utbetalingsinfo (type og beløp) direkte på sak-tabellen.
-- Utbetaling-raden opprettes nå kun ved ferdigstilling, ikke under løpende behandling.
-- Forhandstilsagn-tabellen fjernes og verdiene flyttes til sak.

-- Steg 1: Legg til nye felter på sak
ALTER TABLE sak
    ADD COLUMN utbetalings_type VARCHAR(50),
    ADD COLUMN belop            INTEGER;

-- Steg 2: Populer fra utbetaling (BRUKER-type)
UPDATE sak s
SET utbetalings_type = 'BRUKER',
    belop             = u.belop
FROM utbetaling u
WHERE u.sak_id = s.sak_id;

-- Steg 3: Populer fra forhandtilsagn (FORHANDSTILSAGN-type) – overstyrer BRUKER hvis begge finnes
UPDATE sak s
SET utbetalings_type = 'FORHANDSTILSAGN',
    belop             = f.belop
FROM forhandtilsagn f
WHERE s.forhandtilsagn_id = f.forhandtilsagn_id;

-- Steg 4: Fjern forhandtilsagn_id FK og kolonne fra sak
ALTER TABLE sak
    DROP CONSTRAINT IF EXISTS FK_SAK_ON_FORHANDSTILSAGN;

ALTER TABLE sak
    DROP COLUMN IF EXISTS forhandtilsagn_id;

-- Steg 5: Slett forhandtilsagn-tabellen
DROP TABLE IF EXISTS forhandtilsagn;
