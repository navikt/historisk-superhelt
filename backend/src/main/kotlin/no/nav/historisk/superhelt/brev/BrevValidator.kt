package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.infrastruktur.validation.Validator
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus

class BrevValidator(private val brev: Brev) : Validator() {
    fun checkBrev(): BrevValidator {
        check(brev.tittel.isNullOrBlank(), "tittel", "Brev må ha en tittel")
        check(brev.innhold.isEmptyHtml(), "innhold", "Brev må ha innhold")
        return this
    }

    private fun String?.isEmptyHtml(): Boolean {
        val stripped = this?.replace(Regex("<[^>]*>"), "")?.trim()
        return stripped.isNullOrEmpty()
    }

    fun checkKanSendes(sak: Sak): BrevValidator {
        when (sak.status) {
            SakStatus.UNDER_BEHANDLING -> {
                check(brev.type == BrevType.VEDTAKSBREV, "status", "Vedtaksbrev kan ikke sendes når saken er under behandling")
            }

            else -> {}
        }

        return this
    }

}