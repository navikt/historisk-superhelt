package no.nav.historisk.superhelt.sak

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException

class SakValidator(private val sak: Sak) {


    private val validator: Validator = Companion.validator

    companion object {
        private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        val validator: Validator = factory.validator
    }
    
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
        validate(newStatus !in validTransitions, "Ugyldig statusovergang fra ${sak.status} til $newStatus")

        return this
    }

    fun validateCompleted(): SakValidator {
        // Sjekker annoteringer på Sak-klassen
        val violations = validator.validate(sak)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }

        with(sak) {
            validate(
                utbetaling == null && forhandstilsagn == null,
                "Det må settes enten utbetaling eller forhandstilsagn før sak kan ferdigstilles"
            )
        }

        return this
    }

    fun validateRettighet(rettighet: SakRettighet): SakValidator {
        validate(!sak.hasRettighet(rettighet), "Manglende rettighet i sak: $rettighet")
        return this
    }


}