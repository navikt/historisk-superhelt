-- Lagrer mottatte Kabal-events for idempotens og revisjon.
-- event_id er unik per event fra Kabal og brukes til å unngå dobbeltprosessering.
CREATE TABLE kabal_event (
    id                       UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    event_id                 UUID         NOT NULL UNIQUE,
    saksnummer               VARCHAR(50)  NOT NULL,
    event_type               VARCHAR(100) NOT NULL,
    utfall                   VARCHAR(100),
    tidspunkt                TIMESTAMPTZ  NOT NULL,
    aarsak_feilregistrert    VARCHAR(500),
    journalpost_referanser   TEXT,
    opprettet_tid            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

