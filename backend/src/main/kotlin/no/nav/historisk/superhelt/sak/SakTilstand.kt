package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.infrastruktur.validation.TilstandStatus
/** Representerer tilstanden til en sak i forhold til utfylling og validering
 *
 * Brukes for å vise hvilket steg i en sak som er fullført, har valideringsfeil eller ikke er startet
 */
class SakTilstand(private val sak: Sak) {
    val opplysninger: TilstandStatus
        get() {
            val valideringsfeil = sak.valideringsfeil

            // Sjekk om det ikke er startet på saken i det hele tatt
            if (sak.beskrivelse.isNullOrBlank()
                && sak.begrunnelse.isNullOrBlank()
                && sak.utbetaling == null
                && sak.forhandstilsagn == null
                && sak.vedtaksResultat == null
            ) {
                return TilstandStatus.IKKE_STARTET
            }

            if (!valideringsfeil.isEmpty()) {
                return TilstandStatus.VALIDERING_FEILET
            }
            return TilstandStatus.OK
        }

    val vedtaksbrevBruker: TilstandStatus
        get() {


            if (sak.vedtaksbrevBruker == null || sak.vedtaksbrevBruker.status == BrevStatus.NY) {
                return TilstandStatus.IKKE_STARTET
            }
            val valideringsfeil = sak.vedtaksbrevBruker.valideringsfeil

            if (!valideringsfeil.isEmpty()) {
                return TilstandStatus.VALIDERING_FEILET
            }
            return TilstandStatus.OK
        }

    val oppsummering: TilstandStatus
        get() {
            return when (sak.status) {
                SakStatus.UNDER_BEHANDLING -> TilstandStatus.IKKE_STARTET
                SakStatus.TIL_ATTESTERING -> TilstandStatus.IKKE_STARTET
                SakStatus.FERDIG_ATTESTERT -> TilstandStatus.VALIDERING_FEILET
                SakStatus.FERDIG -> TilstandStatus.OK
                SakStatus.FEILREGISTRERT -> TilstandStatus.IKKE_STARTET
            }
        }


}

