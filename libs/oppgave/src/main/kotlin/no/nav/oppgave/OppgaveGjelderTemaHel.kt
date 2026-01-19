package no.nav.oppgave

import no.nav.oppgave.model.OppgaveDto

/** Tema og type for hva oppgaven gjelder, satt sammen av behandlingstema og behandlingstype fra oppgaven
 *
 * Hentet fra https://oppgave.intern.dev.nav.no/#/Kodeverk/hentGjelderverdierForTema
 * */

enum class OppgaveGjelderTemaHel(
    val behandlingstema: String?,
    val behandlingstemaTerm: String?,
    val behandlingstype: String?,
    val behandlingstypeTerm: String?
) {
    ORTOPEDISKE_HJELPEMIDLER_SOKNAD(
        "ab0013", "Ortopediske hjelpemidler", "ae0034", "Søknad"
    ),
    ANKE(
        null, null, "ae0046", "Anke"
    ),
    KLAGE(
        null, null, "ae0058", "Klage"
    ),
    ORTOPEDISKE_HJELPEMIDLER_UTLAND(
        "ab0013", "Ortopediske hjelpemidler", "ae0106", "Utland"
    ),
    TIDLIGERE_HJEMSENDT_SAK(
        null, null, "ae0114", "Tidligere hjemsendt sak"
    ),
    HJEMSENDT_TIL_NY_BEHANDLING(
        null, null, "ae0115", "Hjemsendt til ny behandling"
    ),
    ORTOPEDISKE_HJELPEMIDLER(
        "ab0013", "Ortopediske hjelpemidler", null, null
    ),
    REISEUTGIFTER(
        "ab0129", "Reiseutgifter", null, null
    ),
    BIDRAG_EKSKL_FARSKAP(
        "ab0328", "Bidrag ekskl. farskap", null, null
    ),
    ANSIKTSDEFEKTSPROTESE(
        "ab0345", "Ansiktsdefektsprotese", null, null
    ),
    BRYSTPROTESE_PROTESEBH(
        "ab0346", "Brystprotese/protesebh", null, null
    ),
    FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER(
        "ab0347", "Fornyelsessøknad ortopediske hjelpemidler", null, null
    ),
    OYEPROTESE(
        "ab0348", "Øyeprotese", null, null
    ),
    PARYKK_HODEPLAGG(
        "ab0349", "Parykk/hodeplagg", null, null
    ),
    REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER(
        "ab0350", "Reisepenger - utprøving ort/tekniske hjelpemidler", null, null
    ),
    PARTSINNSYN(
        null, null, "ae0224", "Partsinnsyn"
    ),
    MEDLEMSKAP(
        "ab0269", "Medlemskap", null, null
    ),
    UKJENT(
    null, "Ukjent", null, null
    );

    fun stringValue(): String {
        return "$behandlingstemaTerm $behandlingstypeTerm"
    }

}

val OppgaveDto.gjelder: OppgaveGjelderTemaHel
    get() = OppgaveGjelderTemaHel.entries.find<OppgaveGjelderTemaHel> { it.behandlingstema == behandlingstema && it.behandlingstype == behandlingstype }
        ?: OppgaveGjelderTemaHel.UKJENT
