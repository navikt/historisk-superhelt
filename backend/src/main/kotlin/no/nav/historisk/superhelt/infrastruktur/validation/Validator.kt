package no.nav.historisk.superhelt.infrastruktur.validation

abstract class Validator {
    private val _validationErrors = mutableListOf<ValidationFieldError>()
    val validationErrors: List<ValidationFieldError>
        get() = _validationErrors.toList()

    protected fun check(condition: Boolean, property: String, message: String) {
        if (condition) {
            _validationErrors.add(ValidationFieldError(property, message))
        }
    }

    /** Sjekker validering og kaster ValideringException hvis feil */
    @Throws(ValideringException::class)
    fun validate() {
        if (_validationErrors.isNotEmpty()) {
            throw ValideringException(reason = "Validering av sak feilet", validationErrors = _validationErrors)
        }
    }

}
