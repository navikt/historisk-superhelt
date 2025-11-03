package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent

class SakValidator(private val sak: Sak) {

    private fun validate(condition: Boolean, message: String) {
        if (condition) {
            throw ValideringException(message)
        }
    }

    fun validateStatusTransition(newStatus: SakStatus): SakValidator {
        val validTransitions = when (sak.status) {
            SakStatus.UNDER_BEHANDLING -> listOf(SakStatus.FERDIG, SakStatus.TIL_ATTESTERING)
            SakStatus.TIL_ATTESTERING -> listOf(SakStatus.FERDIG)
            SakStatus.FERDIG -> listOf(SakStatus.UNDER_BEHANDLING)
        }
        validate(newStatus !in validTransitions, "Ugyldig statusovergang fra) ${sak.status} til $newStatus")

        return this
    }

    fun validateCompleted(): SakValidator {
        with(sak){
            validate(vedtak == null, "Vedtak må være satt før sak kan ferdigstilles")
            validate(tittel == null, "Tittel må være satt før sak kan ferdigstilles")
            //TODO flere valideringer? Evt bruke noen annotasjoner på Sak-klassen
        }

        return this
    }

    fun validateSaksbehandlerErIkkeAttestant(): SakValidator {
        val user = getCurrentNavIdent()
        validate(sak.saksbehandler == user, "Saksbehandler kan ikke attestere egen sak")
        return this
    }


}