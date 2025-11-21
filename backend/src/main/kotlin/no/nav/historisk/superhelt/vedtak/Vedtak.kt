package no.nav.historisk.superhelt.vedtak

import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Fnr
import no.nav.common.types.NavIdent
import no.nav.common.types.NorskeKroner
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.UtbetalingsType
import no.nav.historisk.superhelt.sak.VedtakType
import java.time.Instant
import java.time.LocalDate

/** DTO */
data class Vedtak(
    val saksnummer: Saksnummer,
    val behandlingsnummer: Behandlingsnummer,
    val type: StonadsType,
    val fnr: Fnr,
    val tittel: String, //??
    val soknadsDato: LocalDate,
    val begrunnelse: String? = null,
    val vedtak: VedtakType,
    val vedtaksTidspunkt: Instant,
    val saksbehandler: NavIdent,
    val attestant: NavIdent,
    val utbetalingsType: UtbetalingsType,
    val belop: NorskeKroner?,
//    val vedtaksBrevBruker: JournalpostId,
)
