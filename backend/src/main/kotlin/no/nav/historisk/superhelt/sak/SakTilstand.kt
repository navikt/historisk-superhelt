package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.infrastruktur.exception.ValidationFieldError

class SakTilstand(private val sak: Sak) {
    val soknad: TilstandResultat
        get() {
            val valideringsfeil = SakValidator(sak)
                .checkSoknad()
                .validationErrors

            // Sjekk om det ikke er startet på saken i det hele tatt
            if (sak.tittel.isNullOrBlank()
                && sak.begrunnelse.isNullOrBlank()
                && sak.utbetaling == null
                && sak.forhandstilsagn == null
                && sak.vedtaksResultat == null
            ) {
                return TilstandResultat(
                    tilstand = TilstandStatus.IKKE_STARTET,
                    valideringsfeil = valideringsfeil
                )
            }

                return TilstandResultat(
                    tilstand = if (valideringsfeil.isEmpty()) TilstandStatus.OK else TilstandStatus.VALIDERING_FEILET,
                    valideringsfeil = valideringsfeil
                )

        }

    val vedtaksbrevBruker: TilstandResultat
        get() {
            val valideringsfeil = SakValidator(sak)
                .checkBrev()
                .validationErrors

            if (sak.vedtaksbrevBruker == null || sak.vedtaksbrevBruker.status == BrevStatus.NY) {
                return TilstandResultat(
                    tilstand = TilstandStatus.IKKE_STARTET,
                    valideringsfeil = valideringsfeil
                )
            }

            return TilstandResultat(
                tilstand = if (valideringsfeil.isEmpty()) TilstandStatus.OK else TilstandStatus.VALIDERING_FEILET,
                valideringsfeil = valideringsfeil
            )
        }


    data class TilstandResultat(
        val tilstand: TilstandStatus,
        val valideringsfeil: List<ValidationFieldError>,
    )

    enum class TilstandStatus {
        IKKE_STARTET,
        OK,
        VALIDERING_FEILET
    }
}

