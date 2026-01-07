package no.nav.historisk.superhelt.vedtak

import no.nav.common.types.*
import no.nav.historisk.superhelt.infrastruktur.NavUser
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import java.time.Instant
import java.time.LocalDate

/** DTO */
data class Vedtak(
    val saksnummer: Saksnummer,
    val behandlingsnummer: Behandlingsnummer,
    val stonadstype: StonadsType,
    val fnr: FolkeregisterIdent,
    val beskrivelse: String, //?? Usikker på om denne skal være med
    val soknadsDato: LocalDate,
    val tildelingsAar: Aar?,
    val begrunnelse: String? = null,
    val resultat: VedtaksResultat,
    val vedtaksTidspunkt: Instant,
    val saksbehandler: NavUser,
    val attestant: NavUser,
    val utbetalingsType: UtbetalingsType,
    val belop: Belop?,
//    val vedtaksBrevBruker: JournalpostId,
)
