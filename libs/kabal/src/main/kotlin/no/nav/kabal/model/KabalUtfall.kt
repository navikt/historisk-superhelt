package no.nav.kabal.model

/** Se https://confluence.adeo.no/pages/viewpage.action?pageId=640852224&spaceKey=TeamKlage&title=Introduksjon%2Btil%2BKabal&preview=/640852224/758383696/Utfall%20fra%20Kabal.docx */
enum class KabalUtfall(val lagOppgave: Boolean = true) {
    // Felles utfall
    /** Klageinstans har registrert klagen som trukket av bruker */
    TRUKKET(lagOppgave = false),

    /** Klageinstans har gitt medhold. Vedtaksinstans skal iverksette. */
    MEDHOLD,

    /** Klageinstans har gitt delvis medhold. Vedtaksinstans skal iverksette. */
    DELVIS_MEDHOLD,

    /** Klageinstans har stadfestet vedtaksinstansens vedtak */
    STADFESTELSE(lagOppgave = false),

    /** Betyr at vedtaksinstansen vedtak var ugyldig og er gjort om til brukers ugunst */
    UGUNST,

    /** Klageinstans har avvist klagen fra bruker. Typisk på grunn av at klagefristen er oversittet, ev andre formfeil som at klagen ikke er signert. */
    AVVIST(lagOppgave = false),

    /** Klageinstans kan ikke vurdere saken fordi den ikke er godt nok utredet. Vedtaksenheten skal utrede saken og gjøre nytt vedtak i saken. Saken skal ikke tilbake til oss med mindre bruker klager på vedtaksenhetens nye vedtak. */
    OPPHEVET,

    /** Henlagt betyr at saken ikke behandles noe mer. Det kan ligne på Trukket, men siden det faktisk ikke er trukket av bruker som er forklaringen på hvorfor det ikke skal gjøres videre saksbehandling, er det et eget utfall. Et type tilfelle er at den saken gjelder er død og det er avklart med dødsboet at det ikke blir noen sak som tas videre av dem */
    HENLAGT(lagOppgave = false),

    // Klage-spesifikke
    RETUR,

    // Anke-spesifikke
    HEVET(lagOppgave = false),
    HENVIST(lagOppgave = false),

    // Omgjøringskrav- og gjenopptak-spesifikke
    /** Klageinstans har gitt medhold. Vedtaksinstans skal iverksette. Grunnen til at det heter medhold etter forvaltningsloven § 35 er fordi det er medhold i en sak utenfor ordinær klage- og ankebehandling. */
    MEDHOLD_ETTER_FVL_35,

    GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD,
    GJENOPPTATT_OPPHEVET,

    // Anke i Trygderetten-spesifikke
    INNSTILLING_STADFESTELSE,
    INNSTILLING_AVVIST;

}
