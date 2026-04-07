-- =====================================================
-- TABELL: klage
-- Logg over klager sendt til Kabal.
-- Status oppdaterast via Kafka-topic frå Kabal (kabal.klage-hendelse.v1).
-- =====================================================
CREATE TABLE klage
(
    klage_id                   UUID                     NOT NULL,
    sak_id                     BIGINT                   NOT NULL,
    hjemmel_id                 VARCHAR(100)             NOT NULL,
    dato_klage_mottatt         DATE                     NOT NULL,
    kommentar                  TEXT,
    forrige_behandlende_enhet  VARCHAR(10)              NOT NULL,
    sendt_tidspunkt            TIMESTAMP WITH TIME ZONE NOT NULL,
    status                     VARCHAR(20)              NOT NULL DEFAULT 'SENDT',
    CONSTRAINT pk_klage PRIMARY KEY (klage_id),
    CONSTRAINT fk_klage_sak FOREIGN KEY (sak_id) REFERENCES sak (sak_id)
);

CREATE INDEX idx_klage_sak_id ON klage (sak_id);
CREATE INDEX idx_klage_status  ON klage (status);

