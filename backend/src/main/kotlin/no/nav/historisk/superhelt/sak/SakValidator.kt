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

            check(begrunnelse.isNullOrBlank(), "begrunnelse", "Begrunnelse må være satt")
            check(
                (begrunnelse?.length ?: 0) > 1000,
                "begrunnelse",
                "Begrunnelse kan ikke være lengre enn 1000 tegn"
            )

            check(vedtaksResultat == null, "vedtaksResultat", "Vedtak må være satt")

            if (listOf(VedtaksResultat.INNVILGET, VedtaksResultat.DELVIS_INNVILGET).contains(vedtaksResultat)) {


                when (utbetalingsType) {
                    UtbetalingsType.BRUKER -> {
                        check(
                            belop == null || belop.value <= 0,
                            "utbetaling.belop",
                            "Beløpet må settes og være positivt"
                        )
                        check(
                            !kanUtbetales ,
                            "utbetaling",
                            "Det er ikke mulig å utbetale på sakstype ${type.navn}"
                        )
                    }

                    UtbetalingsType.FORHANDSTILSAGN -> {}
                    UtbetalingsType.INGEN -> {}
                    null -> {
                        check(
                           true,
                            "utbetaling",
                            "Det må velges enten utbetaling eller forhandstilsagn"
                        )
                    }
                }
            }
        }
        return this
    }

    fun checkRettighet(rettighet: SakRettighet): SakValidator {
        check(!sak.rettigheter.contains(rettighet), "rettighet", "Manglende rettighet i sak: $rettighet")
        return this
    }

    /** Krever at saken har minst én av de angitte rettighetene */
    fun checkAnyRettighet(vararg rettigheter: SakRettighet): SakValidator {
        val harNoen = rettigheter.any { sak.rettigheter.contains(it) }
        check(!harNoen, "rettighet", "Manglende rettighet i sak. Krever én av: ${rettigheter.joinToString()}")
        return this
    }

    fun checkUpdate(updateSakDto: UpdateSakDto): SakValidator {
       if (sak.gjenapnet){
           check(updateSakDto.type != null && updateSakDto.type != sak.type, "type", "Kan ikke endre type på en gjenåpnet sak")
           check(updateSakDto.klasseKode != null && updateSakDto.klasseKode != sak.klasseKode, "klassekode", "Kan ikke endre klassekode på en gjenåpnet sak")
       }
        val stonadstype= updateSakDto.type ?: sak.type
        check(updateSakDto.klasseKode != null && !stonadstype.klassekoder.contains(updateSakDto.klasseKode), "klassekode", "Klassekode ${updateSakDto.klasseKode} er ikke gyldig for sakstype ${sak.type}")
        return this
    }
}
