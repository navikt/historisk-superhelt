package no.nav.historisk.superhelt.sak

import no.nav.person.Fnr
import java.time.LocalDate

/** Domeneobjektet */
data class Sak(
    val saksnummer: Saksnummer? = null,
    val type: SaksType,
    val fnr: Fnr,
    val tittel: String? = null,
    val begrunnelse: String? = null,
    val status: SakStatus,
    val opprettetDato: LocalDate = LocalDate.now(),
    val saksbehandler: String,
)
