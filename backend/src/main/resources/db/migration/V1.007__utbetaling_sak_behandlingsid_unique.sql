ALTER TABLE utbetaling
    ADD CONSTRAINT uc_utbetaling_sak_behandlingsnummer UNIQUE (sak_id, behandlingsnummer);
