package no.nav.oppgave

import no.nav.oppgave.model.OppgaveDto

/** Behandlingstema fra oppgave-kodeverk (HEL og HJE)
 * Hentet fra /api/v1/kodeverk/oppgavetype/{tema}
 */

enum class Behandlingstema(val kode: String, val term: String) {

    // Felles for HEL og HJE
    ORTOPEDISKE_HJELPEMIDLER("ab0013", "Ortopediske hjelpemidler"),

    // Spesifikke for HEL
    ANSIKTSDEFEKTSPROTESE("ab0345", "Ansiktsdefektsprotese"),
    BIDRAG_EKSKL_FARSKAP("ab0328", "Bidrag ekskl. farskap"),
    BRYSTPROTESE_PROTESEBH("ab0346", "Brystprotese/protesebh"),
    FORNYELSESSOKNAD_ORTOPEDISKE_HJELPEMIDLER("ab0347", "Fornyelsessøknad ortopediske hjelpemidler"),
    MEDLEMSKAP("ab0269", "Medlemskap"),
    OYEPROTESE("ab0348", "Øyeprotese"),
    PARYKK_HODEPLAGG("ab0349", "Parykk/hodeplagg"),
    REISEPENGER_UTPROVING_ORT_TEKNISKE_HJELPEMIDLER("ab0350", "Reisepenger - utprøving ort/tekniske hjelpemidler"),
    REISEUTGIFTER("ab0129", "Reiseutgifter"),

    // Spesifikke for HJE
    ARBEIDSSTOLER_SITTEMOBLER_OG_BORD("ab0537", "Arbeidsstoler, sittemøbler og bord"),
    ARBEIDS_OG_UTDANNINGSREISER("ab0315", "Arbeids- og utdanningsreiser"),
    BEHANDLINGSBRILLER_LINSER_ORDINAERE_VILKAR("ab0427", "Behandlingsbriller/linser ordinære vilkår"),
    BEHANDLINGSBRILLER_LINSER_SAERSKILTE_VILKAR("ab0428", "Behandlingsbriller/linser særskilte vilkår"),
    BEVEGELSE("ab0536", "Bevegelse"),
    BRILLER_LINSER("ab0317", "Briller/linser"),
    BRILLER_TIL_BARN("ab0420", "Briller til barn"),
    ELEKTRISK_RULLESTOL("ab0539", "Elektrisk rullestol"),
    FAKTURA_HOREAPPARATER("ab0403", "Faktura høreapparater"),
    FILTERBRILLER("ab0523", "Filterbriller"),
    FOLKEHOGSKOLE("ab0368", "Folkehøgskole"),
    FORERHUND("ab0046", "Førerhund"),
    FUNKSJONSASSISTANSE("ab0054", "Funksjonsassistanse"),
    GANGHJELPEMIDDEL("ab0550", "Ganghjelpemiddel"),
    GRUNNMONSTER("ab0242", "Grunnmønster"),
    HENVISNING("ab0378", "Henvisning"),
    HOREAPPARAT("ab0243", "Høreapparat"),
    HOREAPPARAT_DISPENSASJON("ab0445", "Høreapparat - dispensasjon"),
    HORSEL("ab0376", "Hørsel"),
    HYGIENE("ab0562", "Hygiene"),
    INNREDNING_KJOKKEN_OG_BAD("ab0538", "Innredning kjøkken og bad"),
    IRISLINSER("ab0429", "Irislinser"),
    IT("ab0373", "IT"),
    KALENDERE_OG_PLANLEGGINGSVERKTOY("ab0535", "Kalendere og planleggingsverktøy"),
    KJOREPOSE_OG_REGNCAPE("ab0541", "Kjørepose og regncape"),
    KJORERAMPE("ab0542", "Kjørerampe"),
    KOGNISJON("ab0372", "Kognisjon"),
    KOMMUNIKASJON("ab0464", "Kommunikasjon"),
    LESE_OG_SEKRETAERHJELP("ab0245", "Lese- og sekretærhjelp"),
    LESE_OG_SKRIVESTOTTE("ab0566", "Lese- og skrivestøtte"),
    MADRASSER_TRYKKSARFOREBYGGENDE("ab0547", "Madrasser trykksårforebyggende"),
    MANUELL_RULLESTOL("ab0545", "Manuell rullestol"),
    OMBYGGING_TILRETTELEGGING_ARBEID("ab0215", "Ombygging /tilrettelegging arbeid"),
    OMGIVELSESKONTROLL("ab0546", "Omgivelseskontroll"),
    OPPLAERINGSTILTAK("ab0250", "Opplæringstiltak"),
    OVERFLYTTING_VENDING_OG_POSISJONERING("ab0540", "Overflytting, vending og posisjonering"),
    PERSONLOFTER_OG_LOFTESETE("ab0552", "Personløfter og løftesete"),
    REGNING_LESE_OG_SEKRETAERHJELP("ab0443", "Regning lese- og sekretærhjelp"),
    SANSESTIMULERING("ab0558", "Sansestimulering"),
    SENG("ab0548", "Seng"),
    SERVICEHUND("ab0332", "Servicehund"),
    SITTEPUTE("ab0555", "Sittepute"),
    SITTESYSTEM("ab0544", "Sittesystem"),
    STASTATIV("ab0551", "Ståstativ"),
    STOL_MED_OPPREISNING("ab0549", "Stol med oppreisning"),
    SYKKEL("ab0556", "Sykkel"),
    SYN("ab0377", "Syn"),
    TILPASNINGSKURS_DOVE_DOVBLINDE_OG_BLINDE("ab0210", "Tilpasningskurs døve, døvblinde og blinde"),
    TILSKUDD("ab0561", "Tilskudd"),
    TILSKUDD_APPER("ab0560", "Tilskudd apper"),
    TILSKUDD_PC("ab0557", "Tilskudd PC"),
    TILSKUDD_SMAHJELPEMIDLER("ab0559", "Tilskudd småhjelpemidler"),
    TINNITUSMASKERER("ab0253", "Tinnitusmaskerer"),
    TOLK("ab0367", "Tolk"),
    TRAPPEHEIS_OG_LOFTEPLATTFORM("ab0543", "Trappeheis og løfteplattform"),
    VARMEHJELPEMIDDEL("ab0553", "Varmehjelpemiddel"),
    VARSLING_OG_ALARM("ab0370", "Varsling og alarm"),
    VOGN_OG_SPORTSUTSTYR("ab0554", "Vogn og sportsutstyr"),

}

val OppgaveDto.behandlingstemaEnum: Behandlingstema?
    get() = Behandlingstema.entries.find { it.kode == behandlingstema }
