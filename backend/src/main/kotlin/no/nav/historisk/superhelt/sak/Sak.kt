package no.nav.historisk.superhelt.sak

import java.time.LocalDate
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.rest.UtbetalingsType
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.person.Fnr

/** DTO */
data class Sak(
  val saksnummer: Saksnummer,
  val type: StonadsType,
  val fnr: Fnr,
  val tittel: String? = null,
  val soknadsDato: LocalDate? = LocalDate.now(),
  val begrunnelse: String? = null,
  val status: SakStatus,
  val vedtak: VedtakType? = null,
  val opprettetDato: LocalDate = LocalDate.now(),
  val saksbehandler: String,
  val utbetaling: Utbetaling? = null,
  val forhandstilsagn: Forhandstilsagn? = null,
) {
  val utbetalingsType: UtbetalingsType
    get() =
      when {
        forhandstilsagn != null -> UtbetalingsType.FORHANDSTILSAGN
        utbetaling != null -> UtbetalingsType.BRUKER
        else -> UtbetalingsType.INGEN
      }

  val maskertPersonIdent: MaskertPersonIdent
    get() = fnr.toMaskertPersonIdent()
}
