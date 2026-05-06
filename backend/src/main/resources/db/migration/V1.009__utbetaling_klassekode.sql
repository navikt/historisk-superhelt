-- Legg til klassekode som nullable felt på sak og NOT NULL-felt på utbetaling.
-- Utbetaling oppdateres ved å bruke sak.klassekode der det finnes,
-- med fallback til klassekode avledet fra sak.stonads_type.

-- =====================================================
-- TABELL: sak — legg til nullable klassekode
-- =====================================================

ALTER TABLE sak
    ADD COLUMN klassekode VARCHAR(100);

-- =====================================================
-- TABELL: utbetaling — legg til og populer klassekode
-- =====================================================

-- Steg 1: Legg til kolonnen som nullable
ALTER TABLE utbetaling
    ADD COLUMN klassekode VARCHAR(100);

-- Steg 2: Populer klassekode fra sak.klassekode der det finnes,
--         med fallback til klassekode avledet fra sak.stonads_type
UPDATE utbetaling u
SET klassekode = COALESCE(
    s.klassekode,
    CASE s.stonads_type
        WHEN 'PARYKK'         THEN 'PARYKK'
        WHEN 'ANSIKT_PROTESE' THEN 'ANSIKTSDEFEKTPROTESE'
        WHEN 'OYE_PROTESE'    THEN 'ØYEPROTESE'
        WHEN 'BRYSTPROTESE'   THEN 'BRYSTPROTESE'
        WHEN 'FOTTOY'         THEN 'VANLIGE_SKO'
        WHEN 'REISEUTGIFTER'  THEN 'REISEUTGIFTER'
        WHEN 'FOTSENG'        THEN 'FOTSENG'
        WHEN 'PROTESE'        THEN 'ORTOPEDISK_PROTESE'
        WHEN 'ORTOSE'         THEN 'ORTOSE'
        WHEN 'SPESIALSKO'     THEN 'SPESIALSKO'
    END
)
FROM sak s
WHERE s.sak_id = u.sak_id;

-- Steg 3: Sett NOT NULL
ALTER TABLE utbetaling
    ALTER COLUMN klassekode SET NOT NULL;
