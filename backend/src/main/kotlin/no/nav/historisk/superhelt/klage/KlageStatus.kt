package no.nav.historisk.superhelt.klage

/**
 * Status-maskin for klagesending til Kabal.
 *
 * Flyt:
 *   SENDT  ──(Kafka: klage.mottatt)──►  MOTTATT
 *   MOTTATT ──(Kafka: klage.ferdig)───►  FERDIG
 *   SENDT / MOTTATT ──────────────────►  FEILET   (ved feil fra Kabal)
 */
enum class KlageStatus {
    /** Klagen er sendt til Kabal og vi venter på bekreftelse */
    SENDT,

    /** Kabal har bekreftet at klagen er mottatt og opprettet (via Kafka) */
    MOTTATT,

    /** Kabal har ferdigbehandlet klagen (via Kafka) */
    FERDIG,

    /** Noe gikk galt – feilmelding fra Kabal (via Kafka) */
    FEILET,
}

