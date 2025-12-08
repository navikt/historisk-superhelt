package no.nav.historisk.superhelt.endringslogg

import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.sak.Saksnummer
import java.time.Instant

data class EndringsloggLinje(
    val saksnummer: Saksnummer,
    val endretTidspunkt: Instant,
    val type: EndringsloggType,
    val endring: String,
    val beskrivelse: String? = null,
    val endretAv: NavIdent,
)
