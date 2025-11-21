package no.nav.historisk.superhelt.sak

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import no.nav.historisk.superhelt.infrastruktur.exception.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException

class SakValidator(private val sak: Sak) {

    private val _validationErrors = mutableListOf<ValidationFieldError>()
    val validationErrors: List<ValidationFieldError>
        get() = _validationErrors.toList()

    private val validator: Validator = Companion.validator


    companion object {
        private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        val validator: Validator = factory.validator
    }

    private fun check(condition: Boolean, property: String, message: String) {
        if (condition) {
            _validationErrors.add(ValidationFieldError(property, message))
        }
    }

    fun checkStatusTransition(newStatus: SakStatus): SakValidator {
        val validTransitions = when (sak.status) {
            //TODO Fjerne UNDER_BEHANDLING -> FERDIG når totrinnskontroll er på plass
            SakStatus.UNDER_BEHANDLING -> listOf(SakStatus.FERDIG, SakStatus.TIL_ATTESTERING)
            SakStatus.TIL_ATTESTERING -> listOf(SakStatus.FERDIG)
            SakStatus.FERDIG -> listOf(SakStatus.UNDER_BEHANDLING)
        }
        check(newStatus !in validTransitions, "status", "Ugyldig statusovergang fra ${sak.status} til $newStatus")

        return this
    }

    fun checkCompleted(): SakValidator {
        checkSoknad()
        //checkBrev()
        return this
    }

    fun checkSoknad(): SakValidator {
        // Sjekker annoteringer på Sak-klassen
        val violations = validator.validate(sak)
        violations.forEach {
            _validationErrors.add(ValidationFieldError(it.propertyPath.toString(), it.message))
        }

        with(sak) {
            check(
                utbetaling == null && forhandstilsagn == null,
                "utbetaling",
                "Det må settes enten utbetaling eller forhandstilsagn før sak kan ferdigstilles"
            )
        }
        return this
    }

    fun checkRettighet(rettighet: SakRettighet): SakValidator {
        check(!sak.rettigheter.contains(rettighet), "rettighet", "Manglende rettighet i sak: $rettighet")
        return this
    }

    /** Sjekker validering og kaster ValideringException hvis feil */
    @Throws(ValideringException::class)
    fun validate() {
        if (_validationErrors.isNotEmpty()) {
            throw ValideringException(reason = "Validering av sak feilet", validationErrors = _validationErrors)
        }
    }


}