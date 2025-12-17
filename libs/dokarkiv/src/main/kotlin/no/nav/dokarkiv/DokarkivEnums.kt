package no.nav.dokarkiv

enum class JournalpostType {
   INNGAAENDE,
   UTGAAENDE,
   NOTAT,
}

enum class BrukerIdType {
   FNR,
   ORGNR,
   AKTOERID,
}

enum class Filtype {
   PDF,
   JSON,
   XML,
   JPEG,
}

enum class Variantformat {
   ARKIV,
   ORIGINAL,
   FULLVERSJON,
   SLADDET,
}

enum class Sakstype {
   GENERELL_SAK,
   FAGSAK,
}


enum class Kanal {
   NAV_NO,
   ALTINN,
   SKAN_NETS,
   EESSI,
   INNSENDT_NAV_ANSATT,
   HELSENETT,
}


enum class AvsenderMottakerIdType {
   /**
    * Folkeregisterets fødselsnummer eller d-nummer for en person.
    */
   FNR,

   /**
    * Foretaksregisterets organisasjonsnummer for en juridisk person.
    */
   ORGNR,

   /**
    * Helsepersonellregisterets identifikator for leger og annet helsepersonell.
    */
   HPRNR,

   /**
    * Unik identifikator for utenlandske institusjoner / organisasjoner. Identifikatorene vedlikeholdes i EUs institusjonskatalog.
    */
   UTL_ORG,

   /**
    * AvsenderMottakerId er tom.
    */
   NULL,

   /**
    * Ukjent IdType.
    */
   UKJENT,
}
