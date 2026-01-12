package no.nav.historisk.superhelt.brev

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError


data class Brev(
    val saksnummer: Saksnummer,
    val uuid: BrevId,
    val tittel: String?,
    /** html innholdet i brevet */
    val innhold: String?,
    val type: BrevType,
    val mottakerType: BrevMottaker,
    val status: BrevStatus = BrevStatus.NY,
    val journalpostId: EksternJournalpostId? = null,
){
    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val valideringsfeil: List<ValidationFieldError> by lazy { BrevValidator(this).checkBrev().validationErrors }
}

