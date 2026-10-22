import type { Standardtekst } from "@superhelt/breveditor";

export type StandardtekstForSakstype = Omit<Standardtekst, "stonadstype"> & { saksType?: string };

export const standardtekster: StandardtekstForSakstype[] = [
    {
        id: "utbetaling-kontonummer",
        title: "Utbetaling",
        html: "<p>Vi har utbetalt <mark>x xxx kroner</mark> til kontonummeret som er registrert hos Nav.</p>",
        tags: ["Kontonummer"],
    },
    {
        id: "parykk-ordinær-innvilgelse",
        title: "Innvilgelse – stønad for personer over 30 år (ordinær stønad)",
        html: `<p>Du er innvilget stønad til parykk på inntil <mark>6 265</mark> kroner.</p>
<p>Innenfor denne stønadsgrensen kan det også gis stønad til parykk og reparasjon/vedlikehold. Du kan i stedet for parykk kjøpe hodeplagg, hårforlengelse eller hårintegrasjon. Du kan ikke innenfor samme kalenderår få stønad til både parykk/vedlikehold/reparasjon og stønad til hodeplagg/hårforlengelse/hårintegrasjon.</p>
<p>Stønad til parykk følger de satsene som gjelder til enhver tid. Du finner oppdaterte satser på nav.no/satser.</p>
<p>Du har dokumentert kjøp av parykk/hodeplagg til <mark>x xxx</mark> kroner. Maksimal stønad for <mark>20xx</mark> er <mark>6 265</mark> kroner. Vi har utbetalt <mark>x xxx</mark> kroner til kontonummeret som er registrert hos Nav. Refusjonen gjelder for <mark>20xx</mark> og vil være på din konto innen 1-4 virkedager.</p>
<h3>INFORMASJON OM STØNAD</h3>
<p>Stønad til parykk fikk nytt regelverk 1. januar 2026. Du finner oppdatert informasjon på nav.no/parykk.</p>
<p>Du kan fritt velge hvilken leverandør/forhandler du ønsker å benytte. Det forutsettes at leverandøren/forhandleren er registrert med organisasjonsnummer.</p>
<p>Krav om refusjon må sendes til Nav innen seks måneder fra den dagen du betalte for parykken/hodeplagget eller reparasjon/vedlikehold.</p>
<p>Hvis du har betalt selv, og skal søke refusjon:</p>
<p>Du må sende inn spesifisert kvittering på utført betaling og legge ved en eventuell bestillingsbekreftelse/salgskontrakt som viser når varen(e) er bestilt.</p>
<p>Refusjon for varen(e) vil gjelde for det kalenderåret bestillingen er gjennomført, uavhengig av om betaling skjer helt eller delvis på et senere tidspunkt.</p>
<p>Du finner søknadsskjema på nav.no/parykk</p>
<p>Hvis leverandør skal sende faktura til Nav:</p>
<p>Du kan, etter avtale med leverandør, gi fullmakt til at leverandøren sender faktura til Nav. Fullmakten må være signert og inneholde informasjon om hva du har mottatt.</p>
<p>Reparasjon og vedlikehold:</p>
<p>Det kan gis stønad til reparasjon og vedlikehold av parykk innenfor den årlige stønadssatsen. Stønaden skal kun dekke tiltak som gjelder selve parykken.</p>
<p>Frakt, toll og andre gebyrer dekkes ikke. Momsutgifter kan dekkes opp til stønadssatsen ut fra spesifisert kvittering.</p>
<p>For mer informasjon om stønaden og dekning av reiseutgifter se nav.no/parykk</p>`,
        tags: ["Over 30 år", "Ordinær stønad", "Refusjon", "Kvittering", "Fullmakt"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-individuelt-tilpasset-innvilgelse",
        title: "Innvilgelse – individuelt tilpasset parykk (utvidet stønad)",
        html: `<p>Du er innvilget stønad til individuelt tilpasset parykk på inntil <mark>16 220</mark> kroner.</p>
<p>Det er dokumentert at du har nødvendig behov for individuelt tilpasset parykk etter bestemte mål og materialkrav på grunn av avvikende hodeform/allergisk reaksjon mot syntetisk materiale.</p>
<p>Stønad til parykk følger de satsene som gjelder til enhver tid. Du finner oppdaterte satser på nav.no/satser.</p>
<p>Innenfor denne stønadsgrensen kan det også gis stønad til individuelt tilpasset parykk og reparasjon/vedlikehold. Du kan i stedet for parykk kjøpe hodeplagg, hårforlengelse eller hårintegrasjon opp til <mark>6 265</mark> kroner. Det kan ikke ytes stønad til både parykk/reparasjon og hodeplagg/hårforlengelse/hårintegrasjon innenfor samme kalenderår.</p>
<p>Du har dokumentert kjøp av parykk til <mark>xx xxx</mark> kroner. Maksimal stønad for <mark>20xx</mark> er <mark>16 220</mark> kroner. Vi har utbetalt <mark>xx xxx</mark> kroner til kontonummeret som er registrert hos Nav. Refusjonen gjelder for <mark>20xx</mark> og vil være på din konto innen 1-4 virkedager.</p>
<p>Du har for <mark>20xx</mark> valgt å kjøpe hodeplagg/hårforlengelse/hårintegrasjon. Maksimal stønad for <mark>20xx</mark> er <mark>6 265</mark> kroner. Vi har utbetalt <mark>x xxx</mark> kroner til kontonummeret som er registrert hos Nav. Refusjonen gjelder for <mark>20xx</mark> og vil være på din konto innen 1-4 virkedager.</p>
<h3>INFORMASJON OM STØNAD</h3>
<p>Stønad til parykk fikk nytt regelverk 1. januar 2026. Du finner oppdatert informasjon på nav.no/parykk.</p>
<p>Du kan fritt velge hvilken leverandør/forhandler du ønsker å benytte. Det forutsettes at leverandøren/forhandleren er registrert med organisasjonsnummer.</p>
<p>Krav om refusjon må sendes til Nav innen seks måneder fra den dagen du betalte for parykken/hodeplagget eller reparasjon/vedlikehold.</p>
<p>Hvis du har betalt selv, og skal søke refusjon:</p>
<p>Du må sende inn spesifisert kvittering på utført betaling og legge ved en eventuell bestillingsbekreftelse/salgskontrakt som viser når varen(e) er bestilt.</p>
<p>Hvis du kjøper individuelt tilpasset parykk må dette fremkomme av kvittering og kjøpskontrakt/bestillingsbekreftelse.</p>
<p>Refusjon for varen(e) vil gjelde for det kalenderåret bestillingen er gjennomført, uavhengig av om betaling skjer helt eller delvis på et senere tidspunkt.</p>
<p>Du finner søknadsskjema på nav.no/parykk</p>
<p>Hvis leverandør skal sende faktura til Nav:</p>
<p>Du kan, etter avtale med leverandør, gi fullmakt til at leverandøren sender faktura til Nav. Fullmakten må være signert og inneholde informasjon om hva du har mottatt.</p>
<p>Reparasjon og vedlikehold:</p>
<p>Det kan gis stønad til reparasjon og vedlikehold av parykk innenfor den årlige stønadssatsen. Stønaden skal kun dekke tiltak som gjelder selve parykken.</p>
<p>Frakt, toll og andre gebyrer dekkes ikke. Momsutgifter kan dekkes opp til stønadssatsen ut fra spesifisert kvittering.</p>
<p>For mer informasjon om stønaden og dekning av reiseutgifter se nav.no/parykk</p>`,
        tags: ["Individuelt tilpasset", "Allergi", "Syntetisk materiale", "Hodeplagg", "Refusjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-under-30-innvilgelse",
        title: "Innvilgelse – parykk under 30 år",
        html: `<p>Du får stønad til parykk for de under 30 år på inntil <mark>50 000</mark> kroner.</p>
<p>Stønad til parykk følger de satsene som gjelder til enhver tid. Du finner oppdaterte satser på nav.no/satser.</p>
<p>Det kan gis stønad til en eller flere parykker i løpet av et kalenderår opp til stønadsgrensen. Det kan ikke gis stønad til flere parykker enn det som anses nødvendig. Du velger selv leverandør, type parykk og kvalitet. Innenfor denne stønadsgrensen kan det også gis stønad til delparykk og reparasjon/vedlikehold.</p>
<p>Du kan i stedet for parykk kjøpe hodeplagg, hårforlengelse eller hårintegrasjon opp til <mark>6 265</mark> kroner. Det kan ikke ytes stønad til både parykk/reparasjon og hodeplagg/hårforlengelse/hårintegrasjon innenfor samme kalenderår.</p>
<p>Du har dokumentert kjøp av parykk på <mark>xx xxx</mark> kroner. Vi har utbetalt <mark>xx xxxx</mark> kroner. Beløpet vil være på kontonummeret som er registrert hos Nav innen 1-4 virkedager. Utbetalingen gjelder for <mark>20xx</mark>.</p>
<p>Du har for <mark>20xx</mark> valgt å kjøpe hodeplagg/hair extensions. Maksimal stønad for <mark>20xx</mark> er <mark>6 265</mark> kroner. Vi har utbetalt <mark>x xxx</mark> kroner til kontonummeret som er registrert hos Nav. Refusjonen gjelder for <mark>20xx</mark> og vil være på din konto innen 1-4 virkedager.</p>
<p>Fra og med dagen etter du fyller 30 år gjelder de ordinære satsene for parykk.</p>
<h3>INFORMASJON OM STØNAD</h3>
<p>Stønad til parykk fikk nytt regelverk 1. januar 2026. Du finner oppdatert informasjon på nav.no/parykk.</p>
<p>Du kan fritt velge hvilken leverandør/forhandler du ønsker å benytte. Det forutsettes at leverandøren/forhandleren er registrert med organisasjonsnummer.</p>
<p>Krav om refusjon må sendes til Nav innen seks måneder fra den dagen du betalte for parykken/hodeplagget eller reparasjon/vedlikehold.</p>
<p>Hvis du har betalt selv, og skal søke refusjon:</p>
<p>Du må sende inn spesifisert kvittering på utført betaling og legge ved en eventuell bestillingsbekreftelse/salgskontrakt som viser når varen(e) er bestilt.</p>
<p>Refusjon for varen(e) vil gjelde for det kalenderåret bestillingen er gjennomført, uavhengig av om betaling skjer helt eller delvis på et senere tidspunkt.</p>
<p>Du finner søknadsskjema på nav.no/parykk</p>
<p>Hvis leverandør skal sende faktura til Nav:</p>
<p>Du kan, etter avtale med leverandør, gi fullmakt til at leverandøren sender faktura til Nav. Fullmakten må være signert og inneholde informasjon om hva du har mottatt.</p>
<p>Reparasjon og vedlikehold:</p>
<p>Det kan gis stønad til reparasjon og vedlikehold av parykk innenfor den årlige stønadssatsen. Stønaden skal kun dekke tiltak som gjelder selve parykken.</p>
<p>Frakt, toll og andre gebyrer dekkes ikke. Momsutgifter kan dekkes opp til stønadssatsen ut fra spesifisert kvittering.</p>
<p>For mer informasjon om stønaden og dekning av reiseutgifter se nav.no/parykk</p>`,
        tags: ["Under 30 år", "Hodeplagg", "Reparasjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-dod-bruker",
        title: "Innvilgelse – parykk, død bruker",
        html: `<p>Nav mottok den <mark>xx.xx.xx</mark> søknad om dekning av utgifter til parykk for <mark>202x</mark>. Søknaden er innvilget.</p>
<p>Vedtaket er gjort etter folketrygdloven paragraf 10-7 bokstav i med forskrift.</p>
<p>Stønaden utgjør inntil <mark>6 265</mark> kroner for <mark>20xx</mark>. Innenfor denne stønadsgrensen kan det også gis stønad til delparykk, hårdel og reparasjon/vedlikehold.</p>
<h3>UTBETALING</h3>
<p>Det er sendt inn en fullmakt som gir Nav mulighet til å utbetale stønad direkte til leverandør.</p>
<p>Dersom dødsbo har kvitteringer på utlegg til parykk/hodeplagg avdøde har lagt ut for selv, kan disse sendes inn til Nav. Krav om refusjon av utgifter må fremsettes innen seks måneder fra anskaffelsesdato.</p>
<p>Det er dokumentert kjøp av parykk/hodeplagg på <mark>x xxx</mark> kroner. Nav vil utbetale <mark>x xxx</mark> kroner. Refusjon vil utbetales til det kontonummeret som er registrert på avdøde.</p>`,
        tags: ["Dødsbo", "Fullmakt", "Utbetaling", "Refusjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-varsel-frem-i-tid",
        title: "Varsel frem i tid – parykk",
        html: "<p>Parykk anses anskaffet i det kalenderåret den er bestilt. Du har tidligere fått stønad til parykk for det kalenderåret du har betalt parykken i sin helhet. Ved fremtidige søknader får du stønad for det kalenderåret parykken er bestilt, uavhengig av om betaling skjer helt eller delvis på et senere tidspunkt.</p>",
        tags: ["Bestilling", "Kalenderår", "Betaling"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-ikke-godkjent-rekvirent",
        title: "Avslag – parykk, ikke godkjent rekvirent",
        html: `<p>Ved første gangs anskaffelse av parykk/hodeplagg må behovet være dokumentert av relevant helsepersonell. Hvis behovet skyldes alopecia må dette være dokumentert av hudlege (dermatolog).</p>
<p>Du har lagt ved legeerklæring fra spesialist i allmennmedisin om ditt behov for parykk. Du har ikke tidligere mottatt stønad til parykk og det foreligger ikke dokumentasjon fra hudlege (dermatolog)</p>
<p>Søknaden er derfor avslått.</p>
<p>Du kan sende inn ny søknad til Nav med dokumentasjon fra hudlege (dermatolog). Diagnose, omfang og varighet av hårtapet må fremgå av legeerklæringen.</p>`,
        tags: ["Rekvirent", "Alopecia", "Hudlege", "Legeerklæring"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-varighetsvilkar",
        title: "Avslag – parykk, varighetsvilkår ikke oppfylt",
        html: `<p>For å få stønad til parykk må hårtapet være varig. Det vil si at hårtapet har vart eller forventes å vare i minst to år.</p>
<p>Det kan gjøres unntak fra varighetsvilkåret hvis hårtapet skyldes behandling av sykdom.</p>
<p><mark>(Skriv hva vi har mottatt i saken, samt redegjøre for vår vurdering rundt dokumentasjonen)</mark></p>
<p>Vi har vurdert søknaden din om stønad til parykk. For å få stønad må hårtapet ditt ha vart, eller være forventet å vare, i minst to år.</p>
<p>I saken din er det ikke dokumentert at hårtapet har vart så lenge, eller at det kommer til å vare i minst to år.</p>
<p>Vi har vurdert at vilkår om varighet ikke er oppfylt. Du har ikke rett til stønad til parykk, og søknaden din er avslått.</p>`,
        tags: ["Hårtap", "Varighetsvilkår", "Minst to år"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-individuelt-tilpasset-allergi",
        title: "Avslag – individuelt tilpasset parykk, allergi",
        html: `<p>Du har søkt om stønad til individuelt tilpasset parykk på grunn av allergi.</p>
<p>Individuelt tilpasset parykk kan gis hvis det er nødvendig med individuelt tilpasset parykk på grunn av avvikende hodeform eller allergisk reaksjon mot syntetiske fibre i selve parykken.</p>
<p>Individuelt tilpasset innebærer at parykken må være laget etter bestemte mål og materialkrav. Hvis årsaken til plagene er noe annet enn allergi mot syntetisk materiale, kan behovet ikke begrunne stønad til parykk av ekte hår etter egen sats.</p>
<p>Ved allergisk reaksjon mot syntetisk materiale er det et vilkår at relevant kontaktallergi er dokumentert av dermatolog (hudlege) eller spesialavdeling ved sykehus. Dokumentasjonen må beskrive årsakssammenheng mellom syntetiske fibre og allergi. Det må utelukkes at symptomene skyldes reaksjon av andre årsaker, som for eksempel ulike typer lim/teip eller annet material. Dokumentasjonen må derfor tydelig vise at reaksjonen gjelder parykkmaterialet, og ikke tilbehør eller produkter som brukes ved montering.</p>
<p><mark>(Skriv inn relevant tekst: F.eks Du mottar ordinær stønad til parykk og har nå søkt om utvidet stønad til parykk grunnet allergi. Lege skriver du reagerer med utslett/kløe ved bruk av syntetisk parykk.)</mark></p>
<p>I saken din er det ikke dokumentert at du er allergisk mot syntetiske fibre i parykk.</p>
<p>Vi har vurdert at vilkår om stønad til individuelt tilpasset parykk ikke er oppfylt. Du har ikke rett til stønad til individuelt tilpasset parykk, og søknaden din er avslått.</p>`,
        tags: ["Allergi", "Syntetiske fibre", "Kontaktallergi", "Hudlege"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-utvidet-stonad-hodeform",
        title: "Avslag – utvidet stønad, avvikende hodeform",
        html: `<p>Du har søkt om stønad til individuelt tilpasset parykk på grunn av avvikende hodeform.</p>
<p>Individuelt tilpasset parykk kan gis hvis det er nødvendig med individuelt tilpasset parykk på grunn av avvikende hodeform eller allergisk reaksjon mot syntetiske fibre i selve parykken.</p>
<p>Individuelt tilpasset innebærer at parykken må være laget etter bestemte mål og materialkrav.</p>
<p>Avvikende hodeform må avvike så mye fra normal fasong at standardparykker ikke kan tilpasses på en forsvarlig måte. Det er avgjørende at det er årsakssammenheng mellom hodeform og behovet for individuelt tillaget parykk.</p>
<p>Selv om du har en hodeform som skiller seg fra det som er vanlig, må vi også vurdere om det er nødvendig med en individuelt tillaget parykk.</p>
<p>For at du skal få stønad til en individuelt tillaget parykk, må det være dokumentert at en vanlig, prefabrikkert parykk produsert i standardstørrelsene ikke kan brukes.</p>
<p>Hvis behovet ditt kan dekkes godt nok med rimeligere alternativer, kan vi ikke innvilge stønad etter egen sats.</p>
<p>For at vi skal kunne vurdere om du trenger en individuelt tillaget parykk, må legen dokumentere hvordan hodeformen din er avvikende, og forklare hvorfor dette gjør at prefabrikkerte parykker ikke passer.</p>
<p><mark>Forslag:</mark></p>
<p>Det foreligger ikke medisinske opplysninger som sier noe om hvordan hodefasongen din er avvikende ut over at du har en hodeomkrets på <mark>xx</mark> cm. Stort eller lite hode omfattes ikke av unntaket om avvikende hodefasong.</p>
<p>At hårtapet er lokalisert til deler av hodet, som gjør at man velger å benytte hårdel, gir ikke rett til utvidet stønad.</p>
<p><mark>Skriv inn relevant tekst: F.eks Du mottar ordinær stønad til parykk og har nå søkt om utvidet stønad til parykk grunnet avvikende hodeform. Lege skriver du reagerer med utslett/kløe ved bruk av syntetisk parykk.</mark></p>
<p>I saken din er det ikke dokumentert at du har avvikende hodeform og ikke kan bruke prefabrikkert parykk i standardstørrelser.</p>
<p>Vi har vurdert at vilkår om stønad til individuelt tillaget parykk ikke er oppfylt. Du har ikke rett til stønad til individuelt tillaget parykk, og søknaden din er avslått.</p>`,
        tags: ["Avvikende hodeform", "Standardparykker", "Individuelt tilpasset"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-tynt-har",
        title: "Avslag – tynt hår/diffust hårtap",
        html: `<p>Stønad til parykk kan gis til personer som helt eller delvis har mistet håret som følge av sykdom, skade, lyte eller behandling for sykdom</p>
<p>Uavhengig av årsaken til hårtapet må hårtapet være så omfattende at parykk er nødvendig. Flekkvis hårtap som kan dekkes med hestehale, spenner eller lignende, oppfyller som hovedregel ikke nødvendighetsvilkåret. Kort eller tynt hår gir i seg selv ikke rett til stønad til parykk.</p>
<p>For å få stønad til parykk må hårtapet være varig. Det vil si at hårtapet har vart eller forventes å vare i minst to år. Det kan gjøres unntak fra varighetsvilkåret hvis hårtapet skyldes behandling av sykdom.</p>
<p><mark>[Fyll inn konkret for sak: Vedlagt søknaden er det legeerklæring/attest fra xxxxxlege ved xxxxxavdeling. Det fremkommer at du har hårtap som er knyttet til tidligere kreftbehandling/xxxxxxxxxx/har sykdom /benytter medisiner hvor hårtap er del av sykdomsbildet og evt bivirkning av medisinene du bruker. Du har over tid hatt diffust hårtap/uttynnet hår/tynt hår osv..</mark></p>
<p>I saken din er det ikke dokumentert at du har et hårtap som er så omfattende at vi vurder at parykk er nødvendig.</p>
<p>Vi har vurdert at vilkår om stønad til parykk ikke er oppfylt. Du har ikke rett til stønad til parykk, og søknaden din er avslått.</p>`,
        tags: ["Tynt hår", "Diffust hårtap", "Nødvendighetsvilkår"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-kjonnsinkongruens-hartap",
        title: "Avslag – kjønnsinkongruens, ikke dokumentert hårtap",
        html: `<p>Du søker om stønad til parykk fordi du har kjønnsinkongruens.</p>
<p>Du kan få stønad til parykk hvis du helt eller delvis har mistet håret. Behovet må dokumenteres med erklæring fra lege eller annet helsepersonell med nødvendig medisinsk kompetanse.</p>
<p>For å få stønad til parykk må hårtapet være varig. Det vil si at hårtapet har vart eller forventes å vare i minst to år.</p>
<p>I tillegg må hårtapet være så omfattende at bruk av parykk er nødvendig.</p>
<p><mark>(Beskriv hvilke opplysninger som er mottatt og vår vurdering av dem)</mark></p>
<p><mark>Velg enten eller(er det dokumentert et hårtap, men ikke omfang, velg alt. 1. Er det ikke dokumentert hårtap, velg alt. 2):</mark></p>
<p><mark>I saken din er det ikke dokumentert at du har et hårtap som er så omfattende at vi vurder at parykk er nødvendig.</mark></p>
<p><mark>I saken din er det ikke dokumentert at du har et hårtap som gjør at vi vurder at parykk er nødvendig.</mark></p>
<p>Vi har vurdert at vilkår om stønad til parykk ikke er oppfylt. Du har ikke rett til stønad til parykk, og søknaden din er avslått.</p>`,
        tags: ["Kjønnsinkongruens", "Hårtap", "Varighet"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-kjop-privatperson",
        title: "Avslag – refusjon ved kjøp hos privatperson",
        html: `<p>Du har søkt om refusjon av utgifter til hodeplagg/parykk som du har kjøpt av en privatperson.</p>
<p>Utgifter kan dekkes enten som refusjon til deg eller som direkte oppgjør mellom Nav og leverandør/forhandler etter fullmakt fra deg. For at Nav skal kunne gi refusjon må dokumentasjonen oppfylle kravene i gjeldende regelverk.</p>
<p>Ved refusjon må det legges fram spesifisert kvittering fra leverandør som inneholder:</p>
<ul><li>leverandørens organisasjonsnummer</li><li>spesifisering av varen</li><li>opplysninger om betaling</li></ul>
<p>Nav dekker ikke frakt, toll eller andre gebyrer.</p>
<p>I denne saken er det ikke lagt fram dokumentasjon som oppfyller kravene. Det foreligger ikke spesifisert kvittering fra leverandør med organisasjonsnummer. Kjøp gjort av privatperson gir ikke tilstrekkelig grunnlag for refusjon.</p>
<p>Din søknad om refusjon av utgifter til hodeplagg/parykk avslås.</p>`,
        tags: ["Kjøp hos privatperson", "Kvittering", "Organisasjonsnummer", "Refusjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-avslag-refusjon-over-seks-maneder",
        title: "Avslag – refusjon over 6 mnd",
        html: `<p>Du søker om refusjon for kjøp gjort den <mark>xx.xx.xx</mark> på <mark>x xxx</mark> kroner og kjøp gjort <mark>xx.xx.xx</mark> på <mark>x xxx</mark> kroner. Kjøpet/kjøpene er gjort mer enn seks måneder før du sendte inn søknaden om refusjon.</p>
<p>Du fikk innvilget stønad til parykk/hodeplagg i vedtak datert <mark>xx.xx.xx</mark>. I vedtaket ble du informert om at krav om refusjon må sendes inn innen fristen. Du har nå søkt om refusjon av utgiftene.</p>
<p>Krav om refusjon må settes fram senest seks måneder etter at varen er betalt for at Nav kan vurdere om utgiftene kan refunderes. Søknaden din ble sendt inn etter at fristen var gått ut, og regnes derfor som fremsatt for sent.</p>
<p>Nav kan gjøre unntak fra fristen dersom du har vært ute av stand til å fremsette krav innen seks måneder, og det må i så fall være en helt spesiell grunn som gjør at du åpenbart ikke kunne sendt inn kravet innen fristen.</p>
<p><mark>[Kommenter evt. uttalelse fra bruker på hvorfor krav ikke er fremsatt innen frist]</mark></p>
<p>I denne saken er det vurdert at det ikke foreligger en slik spesiell grunn. Det er derfor ikke grunnlag for å gjøre unntak fra fristen. Din søknad om refusjon avslås.</p>
<p>Stønad til parykk er hjemlet i folketrygdloven § 10‑7 bokstav i. Vurderingen av fristen for å fremsette krav er gjort etter folketrygdloven § 22‑13.</p>`,
        tags: ["Refusjon", "Seks måneders frist", "Unntak fra fristen"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-hodeform",
        title: "Innhenting – avvikende hodeform",
        html: `<p>Din søknad om stønad til parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>For at vi skal kunne vurdere om du har rett til parykk etter egen sats på inntil <mark>16 220</mark> kroner etter forskrift om ortopediske hjelpemidler § 8, trenger vi mer informasjon fra frisør/leverandør/lege:</p>
<ul>
<li>Vi ber deg fremskaffe uttalelse fra lege som beskriver hvordan og hvorfor hodeformen er avvikende.</li>
<li>Uttalelse fra lege og eventuelt frisør på hvorfor standardparykker ikke kan tilpasses på en forsvarlig måte</li>
<li>Hvorfor en individuelt tillaget parykk er nødvendig i ditt tilfelle? Det må gis en beskrivelse av hvorfor standardparykker ikke kan tilpasses på en forsvarlig måte?</li>
</ul>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet.</p>
<p>Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Avvikende hodeform", "Standardparykker", "Legeerklæring", "Frist"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-allergi",
        title: "Innhenting – parykk, allergi",
        html: `<p>Din søknad om stønad til parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>For at vi skal kunne vurdere om du har rett til parykk etter egen sats på inntil <mark>16 220</mark> kroner etter forskrift om ortopediske hjelpemidler § 8, trenger vi mer informasjon fra frisør/leverandør/lege:</p>
<p>Vi ber deg fremskaffe dokumentasjon fra dermatolog (hudlege) eller spesialavdeling ved sykehus. Dokumentasjonen må beskrive årsakssammenheng mellom syntetiske fibre og allergi. Det må utelukkes at symptomene skyldes reaksjon av andre årsaker, som for eksempel ulike typer lim/teip eller annet material. Dokumentasjonen må derfor tydelig vise at reaksjonen gjelder parykkmaterialet, og ikke tilbehør eller produkter som brukes ved montering.</p>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet.</p>
<p>Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Allergi", "Syntetiske fibre", "Hudlege", "Dokumentasjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-betaling",
        title: "Innhenting – dokumentasjon på betalt parykk/hodeplagg",
        html: `<p>Din søknad om refusjon for kjøp av parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>For at vi skal kunne vurdere om du har rett til refusjon for kjøp av parykk etter forskrift om ortopediske hjelpemidler § 8, trenger vi mer informasjon. Du må sende oss:</p>
<ul><li>Dokumentasjon som viser dato for betaling, hva som er kjøpt, pris og antall.</li>
<li>Dokumentasjonen må vise at det er du som har kjøpt og betalt produktet. Hvis andre har betalt for deg må du redegjøre for dette.</li></ul>
<p>Nav har ikke anledning til å forskuddsbetale for parykk/hodeplagg.</p>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet. Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Betalingsdokumentasjon", "Kvittering", "Refusjon"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-manglende-legeerklaring",
        title: "Innhenting – parykk, manglende legeerklæring",
        html: `<p>Din søknad om stønad til parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>For at vi skal kunne vurdere om du har rett til stønad til parykk etter forskrift om ortopediske hjelpemidler § 8, trenger vi mer informasjon fra lege:</p>
<ul>
<li>Legeerklæring som bekrefter og redegjør for årsak til hårtap.</li>
<li>Legeerklæringen må inneholde:
<ul><li>En detaljert og nøyaktig beskrivelse av hårtapet i ditt tilfelle.</li>
<li>Omfang og utbredelse av hårtapet.</li>
<li>Hvor lenge hårtapet har vart.</li></ul></li>
<li><mark>(ved alopecia er det ved VESENTLIG hårtap regnet som varig, kan være aktuelt å ha med dette punktet ved innhenting)</mark>Lege må beskrive omfanget tydelig og eventuelt sannsynliggjøre varigheten.</li>
</ul>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet.</p>
<p>Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Legeerklæring", "Hårtap", "Alopecia", "Varighet"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-ny-legeerklaring",
        title: "Innhenting – ny legeerklæring etter mange år",
        html: `<p>Din søknad om stønad til parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>Du har tidligere hatt stønad til parykk. For at vi skal kunne vurdere om du fortsatt har rett til stønad til parykk etter forskrift om ortopediske hjelpemidler § 8 jf. Forvaltningsloven § 17, trenger vi ny informasjon fra lege:</p>
<p>Ny legeerklæring som bekrefter årsaken til hårtap. Lege må gi en nøyaktig og detaljert beskrivelse av hvordan hårtapet ditt er nå. Omfang og utbredelse av hårtap, samt varighet av hårtap må beskrives.</p>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet.</p>
<p>Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Ny legeerklæring", "Hårtap", "Varighet", "Omfang"],
        saksType: "PARYKK",
    },
    {
        id: "parykk-innhenting-kjonnsinkongruens-hartap",
        title: "Innhenting – kjønnsinkongruens, ikke dokumentert hårtap",
        html: `<p>Din søknad om stønad til parykk er mottatt den <mark>XX.XX.XX</mark>.</p>
<p>For at vi skal kunne vurdere om du har rett til stønad til parykk etter forskrift om ortopediske hjelpemidler § 8, trenger vi mer informasjon fra lege:</p>
<ul><li>Legeerklæring som bekrefter hvilken medisinsk diagnose som gir hårtap. Lege må gi en nøyaktig og detaljert beskrivelse av hvordan hårtapet er i ditt tilfelle. Omfang og utbredelse av hårtap, samt varighet av hårtap må beskrives.</li></ul>
<h3>HVA SKJER HVIS DU IKKE SENDER OPPLYSNINGENE?</h3>
<p>Du har en plikt til å gi oss de opplysninger vi trenger for å behandle søknaden din. Hvis vi ikke får opplysningene innen frist, vil vi avgjøre saken med de opplysninger vi har. Det kan bety at søknaden din blir avslått. Dette går fram av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>FRIST</h3>
<p>Etterspurt dokumentasjon må sendes oss innen 3 uker fra du mottok dette brevet.</p>
<p>Har du behov for utsatt frist for innsendelse av etterspurt dokumentasjon, må du ta kontakt med oss via nav.no eller på telefon.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
<h3>SAKSBEHANDLINGSTID</h3>
<p>Søknaden din vil bli behandlet så snart som mulig. Du kan lese mer om saksbehandlingstider på nav.no/saksbehandlingstider.</p>
`,
        tags: ["Kjønnsinkongruens", "Hårtap", "Legeerklæring"],
        saksType: "PARYKK",
    },
    {
        id: "brystprotese-utbetaling-spesialbrystholder",
        title: "Innvilgelse – utbetaling av spesialbrystholder",
        html: `<p>Du får stønad til brystprotese. Ved den første tilpasningen av brystprotese kan det gis stønad til spesialbrystholdere inntil gjeldende sats.</p>
<p>Arbeids- og velferdsdirektoratet skal inngå avtale med leverandører om levering av og priser på brystprotese. Det gis kun stønad til brystprotese som er levert i henhold til slik avtale.</p>
<h3>UTBETALING</h3>
<p>Vi har mottatt kvittering fra deg på <mark>xxx</mark> kroner som gjelder kjøp av spesialbrystholder. Av dette utbetaler vi <mark>xxx</mark> kroner til ditt kontonummer registrert hos Nav. Beløpet vil være på konto innen 1-4 virkedager.</p>
<p>Krav om refusjon for brystprotese og spesialbrystholder må sendes til Nav innen 6 måneder fra anskaffelsestidspunktet. Utgiften skal dokumenteres med spesifisert kvittering.</p>
<h3>INFORMASJON OM REISEUTGIFTER</h3>
<p>Nav dekker refusjon av nødvendige reiseutgifter i forbindelse med anskaffelse, reparasjon, vedlikehold og fornyelse av brystprotese. Reisen dekkes til nærmeste sted protesen kan anskaffes. Reiseutgifter for kjøp av spesialbrystholder dekkes ikke. Søknaden må sendes til Nav innen seks måneder etter at reisen ble foretatt. Søknadsskjema og mer informasjon finner du på nav.no/brystprotese</p>`,
        tags: ["Spesialbrystholder", "Første tilpasning", "Kvittering", "Refusjon", "Reiseutgifter"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-avslag-kompresjonsbrystholder",
        title: "Avslag – kompresjonsbrystholder",
        html: `<p>Etter folketrygdlovens bestemmelser kan du få stønad til løse brystproteser når du har fjernet et bryst på grunn av sykdom. Første gang du får en løs protese kan du også få stønad til en spesialbrystholder. Med spesialbrystholder mener vi her en brystholder som har en egen lomme med plass til den løse brystprotesen. Du kan ikke få stønad til andre brystholdere enn det.</p>
<p>Du har søkt om dekning av utgifter til kompresjonsbrystholder som ledd i behandling etter operasjon. En slik postoperativ kompresjonsbrystholder er ikke å regne som en spesialsbrystholder for protesebrukere. Dette er grunnen til at du har får avslag på søknaden din.</p>
<p>Avslutningsvis vil vi nevne at alt ansvar for postoperativt utstyr, forbruksmateriell og hjelpemidler som brukes i behandlingen er tillagt andre etter annet regelverk, og at du kan rette spørsmål om dekning etter spesialisthelsetjenesteloven til Helseforetaket eller Helfo.</p>`,
        tags: ["Kompresjonsbrystholder", "Postoperativ behandling", "Spesialbrystholder"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-avslag-fjernet-silikonimplantat",
        title: "Avslag – fjernet silikonimplantat",
        html: `<p>Din søknad av <mark>xx.xx.xx</mark> om stønad til brystprotese og spesialbrystholder er avslått.</p>
<h3>BEGRUNNELSE</h3>
<p>Lege har dokumentert at du har fjernet silikonimplantat i bryst og at du nå er helt flatbrystet. På sikt, usikkert når, skal du operere inn nye proteser. Du trenger å få tilpasset proteser i påvente av kirurgisk korreksjon.</p>
<p>Nav kan gi stønad til brystprotese hvis sykdom/skade medfører at bryst helt eller delvis må fjernes. Dette vil for eksempel være aktuelt der ett eller begge bryst fjernes som følge av kreft.</p>
<p>Det kan også gis stønad til brystprotese ved ensidig utviklingsanomali. Det vil si dersom det ene brystet mangler, eller har vesentlig ulik bryststørrelse.</p>
<p>Fjerning av silikonimplantat er ikke en sykdomstilstand og gir ikke gi rett til stønad til brystprotese og spesialbrystholder etter folketrygdlovens bestemmelser. Søknaden er derfor avslått.</p>
<p>Vedtaket er gjort etter folketrygdloven paragraf 10-7i med tilhørende forskrift.</p>`,
        tags: ["Silikonimplantat", "Kirurgisk korreksjon", "Brystfjerning", "Utviklingsanomali"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-avslag-ikke-fastsatt-kjonnsinkongruens",
        title: "Avslag – ikke fastsatt diagnose kjønnsinkongruens",
        html: `<p>Søknaden din av <mark>xx.xx.xx</mark> om dekning av utgifter til spesialbrystholder og brystprotese er avslått.</p>
<h3>BEGRUNNELSE</h3>
<p>Vedtaket er gjort etter folketrygdloven paragraf 10-7 bokstav i med tilhørende forskrift.</p>
<p>Du har søkt om stønad til spesialbrystholder og brystprotese på grunn av kjønnsinkongruens. Vi har mottatt erklæring fra <mark>xxxxxxx</mark>, spesialist i <mark>xxxxxxxx/sexolog/psykolog/etc.</mark> som bekrefter at du er henvist/er under utredning for kjønnsinkongruens.</p>
<p>Stønad til brystprotese og spesialbrystholder kan gis til person med kjønnsinkongruens, herunder ikke-binære personer. Det må dokumenteres med erklæring fra lege eller annet helsepersonell som har nødvendig fagkompetanse at du har kjønnsinkongruens og behov for brystprotese og spesialbrystholder. Slikt helsepersonell kan for eksempel være psykolog, sykepleier eller sexolog.</p>
<p>Vurdering:</p>
<p>Etter gjennomgang av din stønadssak, finner vi ikke dokumentert at gjennomført utredning bekrefter kjønnsinkongruens og behov for brystprotese/spesialbrystholder, i tråd med gjeldende regelverk. Det er kun fremskaffet dokumentasjon som viser at du er henvist til slik utredning.</p>
<p>På nåværende tidspunkt fyller du derfor ikke vilkår for å få dekket stønad til brystprotese og spesialbrystholder. Søknaden er på denne bakgrunn avslått.</p>`,
        tags: ["Kjønnsinkongruens", "Utredning", "Helsepersonell", "Diagnose"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-innhenting-legeerklaring-spesial-bh",
        title: "Innhenting – legeerklæring spesial-BH",
        html: `<p><mark>Vis til hva vi har mottatt/mangler</mark></p>
<p><mark>Eksempeltekst:</mark> Vi har mottatt kvittering som viser kjøp av spesialbrystholder. Det er ikke vedlagt medisinsk dokumentasjon som bekrefter ditt behov for brystprotese.</p>
<p>Ved førstegangsanskaffelse av brystprotese er det et krav at det fremlegges legeerklæring som bekrefter at brystprotese er nødvendig og hensiktsmessig.</p>
<p>Utgifter til spesialbrystholdere kan dekkes inntil <mark>875</mark> kroner ved første tilpasning av brystprotese.</p>
<p>Dette må du sende til Nav innen tre uker fra du mottok dette brevet:</p>
<ul><li>Legeerklæring som bekrefter ovennevnte krav.</li></ul>
<h3>HAR DU BEHOV FOR FORLENGET FRIST</h3>
<p>Ved behov for utsatt frist for innsendelse av etterspurt dokumentasjon må du enten logge inn på ditt Nav på www.nav.no eller ringe til Nav på telefon 55 55 33 33.</p>
<p>Søknaden din vil bli behandlet så snart som mulig etter at de etterspurte opplysningene er mottatt. Se oppdaterte saksbehandlingstider på nav.no/saksbehandlingstider.</p>
<h3>SØKNADEN DIN KAN BLI AVSLÅTT</h3>
<p>Hvis vi ikke har mottatt opplysningene innen fristen, vil vi være nødt til å avgjøre saken med de opplysninger vi har. Dette innebærer at søknaden din kan bli avslått. Dette følger av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
`,
        tags: ["Spesial-BH", "Førstegangsanskaffelse", "Legeerklæring", "Første tilpasning"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-innhenting-tidligere-stonad-spesial-bh",
        title: "Innhenting – tidligere fått stønad til spesial-BH",
        html: `<p>Du har den <mark>xx.xx.xx</mark> søkt om støtte til spesialbrystholder. Du har lagt ved kvittering på kjøp av spesialbrystholder. Vi har ikke tilstrekkelig opplysninger for å behandle søknaden.</p>
<p>Vi viser til vårt vedtak av <mark>xx.xx.xx</mark>. Du har allerede mottatt stønad for spesialbrystholder.</p>
<p>Ved førstegangsanskaffelse av brystprotese dekkes utgifter til en eller flere spesialbrystholdere inntil fastsatt stønadssats.</p>
<p>Som første tilpasning regnes den aller første brystprotesen man får stønad til. Senere kjøp av brystprotese regnes ikke som første tilpasning, selv om man benytter en annen type brystprotese enn tidligere.</p>
<p>Anskaffelser etter ny operasjon kan unntaksvis vurderes som en nyanskaffelse. Et vesentlig endret behov må da dokumenteres med ny legeerklæring.</p>
<p>Dersom det er foretatt en ny operasjon så må dette bekreftes av lege. Et vesentlig endret behov må dokumenteres av legen.</p>
<p>Dette må du sende til Nav innen tre uker fra du mottok dette brevet:</p>
<ul><li>Legeerklæring som bekrefter ovennevnte krav.</li></ul>
<h3>HAR DU BEHOV FOR FORLENGET FRIST</h3>
<p>Ved behov for utsatt frist for innsendelse av etterspurt dokumentasjon må du enten logge inn på ditt Nav på www.nav.no eller ringe til Nav på telefon 55 55 33 33.</p>
<p>Søknaden din vil bli behandlet så snart som mulig etter at de etterspurte opplysningene er mottatt. Se oppdaterte saksbehandlingstider på nav.no/saksbehandlingstider.</p>
<h3>SØKNADEN DIN KAN BLI AVSLÅTT</h3>
<p>Hvis vi ikke har mottatt opplysningene innen fristen, vil vi være nødt til å avgjøre saken med de opplysninger vi har. Dette innebærer at søknaden din kan bli avslått. Dette følger av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
`,
        tags: ["Spesialbrystholder", "Spesial-BH", "Ny operasjon", "Vesentlig endret behov", "Legeerklæring"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "brystprotese-innhenting-kjonnsskifte",
        title: "Innhenting – brystprotese, kjønnsskifte",
        html: `<p>Vi har den <mark>xx.xx.xx</mark> mottatt søknad om stønad til brystprotese og/eller spesialbrystholder.</p>
<p>Vi har ikke nok opplysninger for å behandle saken.</p>
<p>Ved førstegangsanskaffelse av brystprotese kreves det at kjønnsinkongruens dokumenteres. Det er ikke utarbeidet eget skjema til dette stønadsformålet. Relevant helsepersonell må imidlertid klart angi behov og årsak samt bekrefte at brystprotese er nødvendig og hensiktsmessig.</p>
<p>Viser til gjeldende regelverk:</p>
<p>«Det må dokumenteres med erklæring fra lege eller annet helsepersonell som har nødvendig fagkompetanse at en har kjønnsinkongruens og behov for brystprotese. Slikt helsepersonell kan for eksempel være psykolog, sykepleier eller sexolog»</p>
<p>Dette må sendes til oss:</p>
<ul><li>Vi må ha erklæring som bekrefter at du har fått diagnosen kjønnsinkongruens og behov for brystprotese.</li></ul>
<p>Etterspurt dokumentasjon må sendes Nav innen tre uker fra du mottok dette brevet.</p>
<h3>HAR DU BEHOV FOR FORLENGET FRIST</h3>
<p>Ved behov for utsatt frist for innsendelse av etterspurt dokumentasjon må du enten logge inn på ditt Nav på www.nav.no eller ringe til Nav på telefon 55 55 33 33.</p>
<p>Søknaden din vil bli behandlet så snart som mulig etter at de etterspurte opplysningene er mottatt. Se oppdaterte saksbehandlingstider på nav.no/saksbehandlingstider.</p>
<h3>SØKNADEN DIN KAN BLI AVSLÅTT</h3>
<p>Hvis vi ikke har mottatt opplysningene innen fristen, vil vi være nødt til å avgjøre saken med de opplysninger vi har. Dette innebærer at søknaden din kan bli avslått. Dette følger av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
`,
        tags: ["Kjønnsinkongruens", "Diagnose", "Helsepersonell", "Legeerklæring"],
        saksType: "BRYSTPROTESE",
    },
    {
        id: "oyeprotese-innvilgelse",
        title: "Innvilgelse – øyeprotese",
        html: `<p>På grunnlag av foreliggende medisinske opplysninger innvilges stønad til øyeprotese.</p>
<p>Utgifter til anskaffelse av øyeprotese/skallprotese dekkes fullt ut.</p>
<p>Det gis imidlertid ikke stønad for å dekke løpende utgifter til hudpreparater så som salver, kremer, såper og andre hjelpemidler.</p>
<h3>UTBETALING</h3>
<p>Krav om refusjon av utgifter må fremsettes innen 6 måneder fra anskaffelsesdato.</p>
<p>Leverandør av øyeprotese kan sende regning direkte til Nav for refusjon. Du må da gi leverandøren fullmakt til å heve beløpet.</p>
<p>Hvis du har lagt ut for øyeprotesen, kan stønaden utbetales direkte til deg. Du må da legge ved spesifisert kvittering.</p>
<p>Du har lagt ved kvittering for kjøp av øyeprotese på <mark>x xxx</mark> kroner. Vi vil utbetale dette til ditt kontonummer registrert hos Nav. Beløpet vil være på konto innen 1-4 virkedager.</p>
<h3>INFORMASJON OM REISEUTGIFTER</h3>
<p>Nav dekker dine nødvendige reiseutgifter til nærmeste leverandør. Reiseutgifter kan også dekkes i forbindelse med reparasjon, vedlikehold og fornyelse. Du får dekket billigste reisemåte med offentlig transport eller dyrere transport hvis det er dokumentert at det nødvendig. Søknad om reiseutgifter må sendes til Nav innen 6 måneder at reisen fant sted. Du finner søknadsskjema og mer informasjon på nav.no/oyeprotese</p>`,
        tags: ["Skallprotese", "Kvittering", "Fullmakt", "Reiseutgifter", "Refusjon"],
        saksType: "OYE_PROTESE",
    },
    {
        id: "oyeprotese-innhenting-legeerklaring",
        title: "Innhenting – øyeprotese, legeerklæring",
        html: `<p>Søknaden din om stønad til øyeprotese er mottatt den <mark>xx.xx.xx</mark> . Vi har ikke nok dokumentasjon/opplysninger for å behandle saken. Ved førstegangsanskaffelse skal det foreligge en legeerklæring som bekrefter behovet.</p>
<p>Vi ber om at følgende sendes til oss innen tre uker fra du mottok dette brevet:</p>
<ul><li>Legeerklæring som bekrefter behovet for øyeprotese</li></ul>
<h3>HAR DU BEHOV FOR FORLENGET FRIST</h3>
<p>Ved behov for utsatt frist for innsendelse av etterspurt dokumentasjon må du enten logge inn på ditt Nav på www.nav.no eller ringe til Nav på telefon 55 55 33 33.</p>
<p>Søknaden din vil bli behandlet så snart som mulig etter at de etterspurte opplysningene er mottatt. Se oppdaterte saksbehandlingstider på nav.no/saksbehandlingstider.</p>
<h3>SØKNADEN DIN KAN BLI AVSLÅTT</h3>
<p>Hvis vi ikke har mottatt opplysningene innen fristen, vil vi være nødt til å avgjøre saken med de opplysninger vi har. Dette innebærer at søknaden din kan bli avslått. Dette følger av folketrygdloven paragraf 21-3 og 10-7.</p>
<h3>SLIK ETTERSENDER DU OPPLYSNINGER</h3>
<p>Følg veiledningen på vedlagte brev om Svar til Nav.</p>
`,
        tags: ["Legeerklæring", "Førstegangsanskaffelse", "Frist"],
        saksType: "OYE_PROTESE",
    },
    {
        id: "ansiktsprotese-innvilgelse",
        title: "Innvilgelse – ansiktsprotese",
        html: `<p>På grunnlag av foreliggende medisinske opplysninger mottatt fra Oslo Universitetssykehus, Rikshospitalet, innvilges stønad til ansiktsdefektprotese.</p>
<p>Utgifter til ansiktsdefektprotese dekkes fullt ut.</p>
<p>Festeanordninger og annet nødvendig tilbehør dekkes fullt ut.</p>
<p>Utgifter til brilleinnfatning og ordinære brilleglass (uten styrke) kan også dekkes når det er dokumentert at brillen/glassene er nødvendig for å feste ansiktsdefektprotesen. Utgifter til brilleglass med styrke eller andre typer spesialbehandlede glass, for eksempel solbrilleglass, dekkes ikke etter denne bestemmelsen.</p>
<p>Det gis ikke stønad for dekning av løpende utgifter til hudpreparater som salver, kremer, såper og andre rensemidler.</p>
<p>Kopi av vedtaket sendt Oslo universitetssykehus.</p>
<h3>UTBETALING</h3>
<p>Hvis du har lagt ut for ansiktsdefektprotesen, kan stønaden utbetales direkte til deg. Du må da legge med kravet en spesifisert kvittering.</p>
<p>Krav om refusjon må fremsettes innen 6 måneder fra anskaffelsesdato.</p>
<p>Hvis du ønsker at leverandør av ansiktsdefektprotesen skal få refundert stønaden, må du signere en fullmakt om at Nav kan utbetale direkte til leverandøren og du må signere for at du har mottatt protesen.</p>
<h3>INFORMASJON OM REISEUTGIFTER</h3>
<p>Nav dekker dine nødvendige reiseutgifter til nærmeste leverandør. Reiseutgifter kan også dekkes i forbindelse med reparasjon, vedlikehold og fornyelse. Du får dekket billigste reisemåte med offentlig transport eller dyrere transport hvis det er dokumentert at det nødvendig. Søknad om reiseutgifter må sendes til Nav innen 6 måneder at reisen fant sted. Du finner søknadsskjema og mer informasjon på nav.no/ansiktsprotese</p>`,
        tags: ["Ansiktsdefektprotese", "Festeanordninger", "Brilleinnfatning", "Refusjon", "Reiseutgifter"],
        saksType: "ANSIKT_PROTESE",
    },
    {
        id: "ansiktsprotese-delvis-innvilgelse",
        title: "Delvis innvilgelse – ansiktsprotese",
        html: `<p>Søknaden din av <mark>xx.xx.23</mark> om dekning av utgifter til ansiktsdefektprotese festet med brille er innvilget.</p>
<p>Søknad om stønad til styrke/progressive glass er avslått.</p>
<p>Vedtaket er gjort etter folketrygdloven paragraf 10-7i med tilhørende forskrift.</p>
<p><mark>På grunnlag av foreliggende medisinske opplysninger innvilges du stønad til ansiktsdefektprotese festet med brille/ Du er innvilget fornyelse av ansiktsdefektprotese festet med brille.</mark></p>
<p>Utgifter til ansiktsdefektprotese dekkes fullt ut.</p>
<p>Festeanordninger og annet nødvendig tilbehør dekkes fullt ut.</p>
<p>Utgifter til brilleinnfatning og ordinære brilleglass (uten styrke) kan dekkes når det er dokumentert at brillen/glassene er nødvendig for å feste ansiktsdefektprotesen. Dette dekkes i ditt tilfelle.</p>
<p>Utgifter til brilleglass med styrke dekkes ikke etter denne bestemmelsen. Stønader til synshjelpemidler behandles av Hjelpemiddelsentralen.</p>
<p>Det gis ikke stønad for dekning av løpende utgifter til hudpreparater som salver, kremer, såper og andre rensemidler.</p>
<p>Kopi av vedtaket sendt Oslo universitetssykehus.</p>
<h3>UTBETALING</h3>
<p>Hvis du har lagt ut for ansiktsdefektprotesen, kan stønaden utbetales direkte til deg. Du må da legge med kravet en spesifisert kvittering.</p>
<p>Krav om refusjon må fremsettes innen 6 måneder fra anskaffelsesdato.</p>
<p>Hvis du ønsker at leverandør av ansiktsdefektprotesen skal få refundert stønaden, må du signere en fullmakt om at Nav kan utbetale direkte til leverandøren og du må signere for at du har mottatt protesen</p>`,
        tags: ["Ansiktsdefektprotese", "Brille", "Brilleglass", "Styrkeglass", "Fornyelse"],
        saksType: "ANSIKT_PROTESE",
    },
    {
        id: "ansiktsprotese-avslag-tannbehandling",
        title: "Avslag – ansiktsprotese, tannbehandling",
        html: `<p>Søknaden din av <mark>xx.xx.xx</mark> om dekning av utgifter til ansiktsdefektprotese er avslått.</p>
<h3>BEGRUNNELSE FOR AVSLAGET</h3>
<p>Det søkes om ansiktsdefektprotese. Det er ikke vedlagt dokumentasjon som bekrefter behov for dette.</p>
<p>Vedlagt søknaden er dokumentasjon fra tannlege og det fremgår at det er snakk om tannprotetisk behandling.</p>
<p>Søknader om tannbehandling etter § 5-6 behandles av HELFO (Helseøkonomiforvaltningen).</p>
<p>Unntaksvis kan utgifter til implantat (titanskrue som er festet til kjeveben) dekkes etter § 10-7 bokstav i, dersom implantatet er nødvendig for å få festet en ansiktsdefektprotese. Så vidt vi kan se har du ikke tidligere vært innvilget ansiktsdefektprotese og det er heller ikke ved denne søknaden vedlagt legeerklæring som dokumenterer behov for dette.</p>
<p>Du får derfor avslag på søknaden, dekning av utgifter til denne tannbehandlingen er ikke lovhjemlet i § 10-7.</p>
<p>HELFO kan kontaktes for spørsmål om tannbehandling.</p>`,
        tags: ["Tannbehandling", "HELFO", "Tannprotetisk behandling", "Implantat"],
        saksType: "ANSIKT_PROTESE",
    },
];
