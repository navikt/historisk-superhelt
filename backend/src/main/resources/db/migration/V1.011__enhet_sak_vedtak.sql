ALTER TABLE sak
    ADD COLUMN enhet VARCHAR(10);

ALTER TABLE vedtak
    ADD COLUMN enhet VARCHAR(10);

-- Sett enhet basert på stonads_type-logikk fra Enheter.guessEnhet:
--   tema = HEL               → 4485 (NAV Arbeid og ytelser)
--   ARBEID_UTDANNING (HJE)   → 0587 (NAV Tiltak Innlandet)
--   øvrige (HJE)             → 9999 (dummy)

UPDATE sak
SET enhet = CASE
                WHEN stonads_type = 'ARBEID_UTDANNING' THEN '0587'
                WHEN stonads_type = 'HOREAPPARAT' THEN '9999'
                ELSE '4485'
    END
WHERE enhet IS NULL;

UPDATE vedtak
SET enhet = CASE
                WHEN stonads_type = 'ARBEID_UTDANNING' THEN '0587'
                WHEN stonads_type = 'HOREAPPARAT' THEN '9999'
                ELSE '4485'
    END
WHERE enhet IS NULL;

ALTER TABLE sak
    ALTER COLUMN enhet SET NOT NULL;

ALTER TABLE vedtak
    ALTER COLUMN enhet SET NOT NULL;