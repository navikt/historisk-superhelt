package no.nav.historisk.superhelt.klage

import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Klage(
    val id: UUID,
    val saksnummer: Saksnummer,

    /** Hjemmel-ID sendt til Kabal, f.eks. "FTRL_10_7I" */
    val hjemmelId: String,

    /** Dato klagen ble mottatt (fra skjema) */
    val datoKlageMottatt: LocalDate,

    /** Valgfri kommentar fra saksbehandler */
    val kommentar: String?,

    /** behandlingId returnert fra Kabal ved vellykket avsending */
    val kabalBehandlingId: String?,

    /** Tidspunkt klagen ble opprettet i Superhelt */
    val opprettetTidspunkt: Instant,

    /** NavIdent til saksbehandleren som sendte klagen */
    val opprettetAv: NavIdent,

    val status: KlageStatus,
)

enum class KlageStatus {
    /** Klagen er sendt til Kabal og mottatt (200 OK) */
    SENDT,

    /** Klagen feilet ved avsending til Kabal */
    FEILET,
}

