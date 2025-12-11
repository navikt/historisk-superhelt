package no.nav.historisk.superhelt.brev

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError


data class BrevUtkast(
    val uuid: BrevId,
    val tittel: String?,
    /** html innholdet i brevet */
    val innhold: String?,
    val type: BrevType,
    val mottakerType: BrevMottaker,
    val status: BrevStatus = BrevStatus.NY
){
    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val validation: List<ValidationFieldError>
        get() = BrevValidator(this).checkBrev().validationErrors
}

