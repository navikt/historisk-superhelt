-- Knytt utbetaling til behandlingsnummer (versjonsnummer i saken).
-- Eksisterende rader får behandlingsnummer = 1 (første behandling).

ALTER TABLE utbetaling
    ADD COLUMN behandlingsnummer INT NOT NULL DEFAULT 1;

ALTER TABLE utbetaling
    ALTER COLUMN behandlingsnummer DROP DEFAULT;
