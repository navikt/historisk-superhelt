package no.nav.dokdist

import no.nav.dokarkiv.EksternJournalpostId

data class DistribuerJournalpostRequest(
    val journalpostId: EksternJournalpostId,
    val bestillendeFagsystem: String, // todo: trenger en type som identifiserer om dette er felles kodeverk eller noe annet.
    val dokumentProdApp: String, // todo: trenger en type som identifiserer om dette er felles kodeverk eller noe annet.
    val distribusjonstype: Distribusjonstype,
    val distribusjonstidspunkt: Distribusjonstidspunkt,
) {
    enum class Distribusjonstype {
        VEDTAK,
        VIKTIG,
        ANNET,
    }

    enum class Distribusjonstidspunkt {
        UMIDDELBART,
        KJERNETID,
    }
}



data class DistribuerJournalpostResponse(
    val bestillingsId: String,
)