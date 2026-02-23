package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.brev.BrevValidator
import no.nav.historisk.superhelt.infrastruktur.validation.Validator
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat


class SakValidator(private val sak: Sak): Validator() {

    fun checkStatusTransition(toStatus: SakStatus): SakValidator {
        val fromStatus = sak.status
        val validTransitions = when (fromStatus) {
            SakStatus.UNDER_BEHANDLING -> listOf( SakStatus.TIL_ATTESTERING, SakStatus.FEILREGISTRERT)
            SakStatus.TIL_ATTESTERING -> listOf(SakStatus.FERDIG_ATTESTERT, SakStatus.UNDER_BEHANDLING)
            SakStatus.FERDIG_ATTESTERT -> listOf(SakStatus.FERDIG)
            SakStatus.FERDIG -> listOf(SakStatus.UNDER_BEHANDLING)
            SakStatus.FEILREGISTRERT -> emptyList()
        }
        check(toStatus !in validTransitions, "status", "Ugyldig statusovergang fra $fromStatus til $toStatus")

        return this
    }

    fun checkCompleted(): SakValidator {
        checkSoknad()
        checkBrev()
        return this
    }

    fun checkBrev() : SakValidator {
        sak.vedtaksbrevBruker?.let { brev ->
            BrevValidator(brev).checkBrev()
                .validationErrors
                .forEach { feil ->
                    check(true, "vedtaksbrevBruker.${feil.field}", feil.message)
                }
        }
        return this
    }

    fun checkSoknad(): SakValidator {
        // TODO vurdere å bruke enum for felt for mer strukturert validering
        with(sak) {

            check(beskrivelse.isNullOrBlank(), "beskrivelse", "Beskrivelse må være satt")
            check((beskrivelse?.length ?: 0) > 200, "beskrivelse", "Beskrivelse kan ikke være lengre enn 200 tegn")

            check(soknadsDato == null, "soknadsDato", "Søknadsdato må være satt")
            check(
                tildelingsAar != null && !tildelingsAar.isValid(),
                "tildelingsAar",
                "Tildelingsår må være et gyldig årstall"
            )

            check(
                (begrunnelse?.length ?: 0) > 1000,
                "begrunnelse",
                "Begrunnelse kan ikke være lengre enn 1000 tegn"
            )

            check(vedtaksResultat == null, "vedtaksResultat", "Vedtak må være satt")

            if (listOf(VedtaksResultat.INNVILGET, VedtaksResultat.DELVIS_INNVILGET).contains(vedtaksResultat)) {
                check(
                    utbetaling == null && forhandstilsagn == null,
                    "utbetaling",
                    "Det må velges enten utbetaling eller forhandstilsagn"
                )

                when (utbetalingsType) {
                    UtbetalingsType.BRUKER -> {
                        check(
                            utbetaling != null && utbetaling.belop.value <= 0,
                            "utbetaling.belop",
                            "Beløpet må settes og være positivt"
                        )
                    }

                    UtbetalingsType.FORHANDSTILSAGN -> {}
                    UtbetalingsType.INGEN -> {}
                }
            }
        }
        return this
    }

    fun checkRettighet(rettighet: SakRettighet): SakValidator {
        check(!sak.rettigheter.contains(rettighet), "rettighet", "Manglende rettighet i sak: $rettighet")
        return this
    }

    fun checkUpdate(updateSakDto: UpdateSakDto): SakValidator {
       if (sak.gjenapnet){
           check(updateSakDto.type != null && updateSakDto.type != sak.type, "type", "Kan ikke endre type på en gjenåpnet sak")
       }
        return this
    }
}