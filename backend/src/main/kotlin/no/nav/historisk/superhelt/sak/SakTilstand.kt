package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.ValidationFieldError

class SakTilstand(private val sak: Sak) {
    val soknad: TilstandResultat
        get() {
            val validationErrors = SakValidator(sak)
                .checkSoknad()
                .validationErrors
            if (validationErrors.isEmpty()) {
                return TilstandResultat(
                    tilstand = TilstandStatus.OK,
                    valideringsfeil = validationErrors
                )
            }
            // Sjekk om det ikke er startet på saken i det hele tatt
            if (sak.tittel.isNullOrBlank()
                && sak.begrunnelse.isNullOrBlank()
                && sak.utbetaling == null
                && sak.forhandstilsagn == null
                && sak.vedtaksResultat == null
            ) {
                return TilstandResultat(
                    tilstand = TilstandStatus.IKKE_STARTET,
                    valideringsfeil = validationErrors
                )
            }

            return TilstandResultat(
                tilstand = TilstandStatus.VALIDERING_FEILET,
                valideringsfeil = validationErrors
            )
        }

    val vedtaksbrev: TilstandResultat
        get() {
            return TilstandResultat(
                tilstand = TilstandStatus.IKKE_STARTET,
                valideringsfeil = emptyList()
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

