package no.nav.kabal.model

data class SendSakV4Response(
    val behandlingId: String,
    val mottattDato: String,
    val journalpostId: String? = null,
    val feilmeldinger: List<String> = emptyList()
)

