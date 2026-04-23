package no.nav.oppgave

import no.nav.oppgave.model.OppgaveDto

/** Behandlingstype fra oppgave-kodeverk (HEL og HJE)
 * Hentet fra /api/v1/kodeverk/oppgavetype/{tema}
 */

enum class Behandlingstype(val kode: String, val term: String) {

    // Felles for HEL og HJE
    ANKE("ae0046", "Anke"),
    KLAGE("ae0058", "Klage"),
    PARTSINNSYN("ae0224", "Partsinnsyn"),
    SOKNAD("ae0034", "Søknad"),
    UTLAND("ae0106", "Utland"),

    // Spesifikke for HEL
    HJEMSENDT_TIL_NY_BEHANDLING("ae0115", "Hjemsendt til ny behandling"),
    TIDLIGERE_HJEMSENDT_SAK("ae0114", "Tidligere hjemsendt sak"),

    // Spesifikke for HJE
    AKTIVITETSHJELPEMIDLER_AKT26("ae0287", "Aktivitetshjelpemidler/AKT26"),
    ARBEIDSLIV("ae0276", "Arbeidsliv"),
    BARN("ae0223", "Barn"),
    BEHANDLE_VEDTAK("ae0004", "Behandle vedtak"),
    BESTILLING("ae0281", "Bestilling"),
    BOLIG("ae0277", "Bolig"),
    BYTTE("ae0285", "Bytte"),
    DAGLIGLIV("ae0278", "Dagligliv"),
    DIGITALT_BYTTE("ae0273", "Digitalt bytte"),
    DIGITAL_SOKNAD("ae0227", "Digital søknad"),
    ETTERSENDELSE("ae0288", "Ettersendelse"),
    HASTEBESTILLING("ae0282", "Hastebestilling"),
    HASTEBYTTE("ae0283", "Hastebytte"),
    HASTESOKNAD("ae0286", "Hastesøknad"),
    HJELP_TIL_UTPROVING("ae0289", "Hjelp til utprøving"),
    TILBEHOR("ae0291", "Tilbehør"),
    UTDANNING("ae0275", "Utdanning"),

}

val OppgaveDto.behandlingstypeEnum: Behandlingstype?
    get() = Behandlingstype.entries.find { it.kode == behandlingstype }
