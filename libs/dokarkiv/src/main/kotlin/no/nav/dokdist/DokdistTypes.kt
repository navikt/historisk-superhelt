package no.nav.dokdist

import no.nav.common.types.EksternJournalpostId

data class DistribuerJournalpostRequest(
    val journalpostId: EksternJournalpostId,
    val bestillendeFagsystem: String,
    val dokumentProdApp: String,
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

data class DokdistRespons(
    val bestillingsId: String? = null,
    val sendtOk: Boolean,
    val feilbegrunnelse: String? = null,
)
