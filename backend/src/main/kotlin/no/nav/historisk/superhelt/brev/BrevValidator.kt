package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.infrastruktur.validation.Validator

class BrevValidator(private val brev: BrevUtkast) : Validator() {
    fun checkBrev(): BrevValidator {
        check(brev.tittel.isNullOrBlank(), "tittel", "Vedtaksbrev til bruker må ha en tittel")
        check(brev.innhold.isEmptyHtml(), "innhold", "Vedtaksbrev til bruker må ha innhold")
        return this
    }

    private fun String?.isEmptyHtml(): Boolean {
        val stripped = this?.replace(Regex("<[^>]*>"), "")?.trim()
        return stripped.isNullOrEmpty()
    }

}