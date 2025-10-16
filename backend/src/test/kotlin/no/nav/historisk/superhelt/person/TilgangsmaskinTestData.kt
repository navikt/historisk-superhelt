package no.nav.historisk.superhelt.person

import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.ProblemDetaljResponse

object TilgangsmaskinTestData {

    val problemDetailResponse = ProblemDetaljResponse(
        type = "type",
        title = Avvisningskode.AVVIST_HABILITET,
        status = 403,
        instance = "instance",
        brukerIdent = "brukeriden",
        navIdent = "navident",
        begrunnelse = "begrunnelse",
        traceId = "traceId",
        kanOverstyres = false
    )
}