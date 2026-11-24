# Backend-arkitektur

Backend er en Kotlin/Spring Boot-applikasjon med REST, PostgreSQL/JPA og Kafka. Denne beskrivelsen viser dagens kode og konkrete avvik fra [felles kodestandard](arkitektur-og-kodestandard.md). Den beskriver eksisterende tilgangsgrenser, men er ikke en sikkerhetsvurdering.

Arkitekturprinsippet er løkarkitektur med DDD som utgangspunkt: domenet er kjernen, applikasjonslaget orkestrerer brukstilfeller, og ytre adaptere håndterer web, persistens og integrasjoner. [Kodestandarden](arkitektur-og-kodestandard.md#løkarkitektur-i-backend) definerer avhengighetsretningen og det pragmatiske unntaket for serialiseringsannotasjoner. Beskrivelsen nedenfor viser hvordan dagens kode følger og avviker fra dette prinsippet.

## Ansvarsgrenser og domenemodell

Domenepakkene ligger under `backend/src/main/kotlin/no/nav/historisk/superhelt/`. Integrasjonsbibliotekene i `libs/` inneholder klienter og meldingstyper; backend konfigurerer dem og setter sammen saksflyten. Felles verdityper som `Saksnummer`, `FolkeregisterIdent` og `Belop` ligger i `libs/common-types`.

| Modell | Rolle i dagens implementasjon |
|--------|--------------------------------|
| [Sak](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/Sak.kt) | Gjeldende tilstand, behandlingsnummer, ett vedtaksresultat og ett beløp |
| [Vedtak](../backend/src/main/kotlin/no/nav/historisk/superhelt/vedtak/Vedtak.kt) | Lagrede avgjørelsesopplysninger knyttet til sak og behandlingsnummer |
| [Utbetaling](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/Utbetaling.kt) | Betalingsopplysninger med transaksjons-ID, status og kobling til sak og behandling |

Domeneobjektene er adskilt fra JPA-modellene. For eksempel representerer `Utbetaling` betalingen i domenet, mens [UtbetalingJpaEntity](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/db/UtbetalingJpaEntity.kt) beskriver persistensen. `Sak` bruker også Jacksons `@JsonProperty` på avledede felter. Slike annotasjoner er ikke i seg selv et arkitekturavvik etter det avklarte serialiseringsunntaket.

[SakJpaEntity](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/db/SakJpaEntity.kt) har en `behandlingsTeller` som starter på 1 og økes ved gjenåpning. Modellen har ingen separat behandling-entitet. [VedtakRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/vedtak/VedtakRepository.kt) lagrer en kopi av avgjørelsesfelter med sakens aktuelle behandlingsteller.

Dette er en forenkling av [domenet i ordlisten](../GLOSSARY.md), der en søknad kan omfatte flere krav, men hvert vedtak gjelder ett krav. Kravets utfall er trukket, vedtatt eller omgjort, og er noe annet enn vedtaksresultatet innvilget, delvis innvilget eller avslått. Et krav under arbeid har ennå ikke noe utfall. Flere vedtak kan underrettes i samme vedtaksbrev etter at behandlingen er avsluttet. Ett resultat og ett beløp i dagens modell er ikke en regel om at domenet bare har ett krav.

[Databasebegrensningen for utbetaling](../backend/src/main/resources/db/migration/V1.007__utbetaling_sak_behandlingsid_unique.sql) tillater én utbetalingsrad per `(sak_id, behandlingsnummer)`. Samme sak kan ha utbetalingsrader fra flere behandlinger. Transaksjons-ID-en er også unik.

## Lagdeling, validering og REST-feil

Den avklarte ansvarsdelingen står i [kodestandarden](arkitektur-og-kodestandard.md#ansvarsdeling-i-backend). I dagens kode gjør [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) også validering og orkestrering som standarden legger til service-/domenelaget.

Repository-er mapper mellom mutable JPA-entities og domenetyper. For eksempel mapper `SakJpaEntity.toDomain()` til en `Sak` med immutable felt. Entity-grensen er likevel ikke konsekvent: `SakRepository.getSakEntityOrThrow()` returnerer en JPA-entity, og blant annet `EndringsloggService` bruker denne utenfor repository-laget.

[REST-DTO-ene](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakRestTypes.kt) bruker Bean Validation-annotasjoner. Noen controller-metoder bruker `@Valid`, mens attestering også sjekker returkommentaren eksplisitt. [SakValidator](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakValidator.kt) kontrollerer rettigheter, fullstendighet og statusoverganger.

[GlobalControllerAdvice](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/GlobalControllerAdvice.kt) oversetter blant annet valideringsfeil til 400, tilgangsnekt til 403 og øvrige uventede feil til 500 med `ProblemDetail`. Frontenden bruker problemfeltene til feilmeldinger.

## Attestering og ferdigstilling

Godkjent attestering går gjennom `SakActionController.attesterSak()`. Metoden henter saken, validerer attesteringsrettigheten og utfører følgende:

1. `attester()` validerer statusovergang og fullstendighet, logger attesteringen og setter status til `FERDIG_ATTESTERT`.
2. `ferdigstill()` sender eventuelt vedtaksbrev gjennom `BrevSendingService`.
3. `UtbetalingService.sendTilUtbetaling()` håndterer eventuell utbetaling eller annullering.
4. Saken settes til `FERDIG`, `VedtakService.fattVedtak()` lagrer vedtaket, og ferdigstillingen logges.
5. Når ferdigstillingen har returnert, ferdigstilles attestantoppgaven.

`ferdigstill()` er også et eget REST-endpoint for rekjøring. Controllerens attestering og ferdigstilling har ingen ytre `@Transactional`. Deloperasjonene har egne grenser; brev, Kafka-sending og databaseendringer er ikke én atomisk operasjon.

Ved retur fra attestering endres saken tilbake til `UNDER_BEHANDLING`, returkommentaren logges og oppgaveflyten oppdateres. Retur fortsetter samme behandling; gjenåpning øker derimot behandlingstelleren.

## Brev, journalføring og distribusjon

[BrevSendingService](../backend/src/main/kotlin/no/nav/historisk/superhelt/brev/BrevSendingService.kt) orkestrerer sending. [BrevService](../backend/src/main/kotlin/no/nav/historisk/superhelt/brev/BrevService.kt) utfører arkivering og distribusjon. De to klassene har dermed ulike roller.

Brevets lagrede statuser er `UTKAST`, `KLAR_TIL_SENDING`, `ARKIVERT` og `SENDT`. Orkestratoren hopper over et allerede sendt brev; ellers klargjør, arkiverer og distribuerer den brevet ut fra lagret tilstand. Arkiveringen lagrer journalpost-ID og kan forsøke å ferdigstille journalposten.

`BrevService.arkiver()` og `distribuer()` bruker `Propagation.NEVER`. Distribusjonskallet skjer før status og endringslogg lagres gjennom en egen `TransactionTemplate`. Ved feil settes et feilflagg og feilen kastes videre.

Statusene gir støtte for å fortsette fra lagret tilstand, men garanterer ikke exactly-once hos eksterne tjenester. Et eksternt kall kan ha lykkes selv om den etterfølgende lokale lagringen feiler. Nye forsøk må vurderes med dette skillet.

[PdfgenService](../backend/src/main/kotlin/no/nav/historisk/superhelt/brev/pdfgen/PdfgenService.kt) mapper sak og brev til forespørsler til den separate pdfgen-tjenesten. Backend er grensen mot nettleseren for HTML og PDF; klienten i `libs/pdfgen` håndterer tjenestekallene. Se [pdfgen-kontrakten](../pdfgen/README.md) for Typst-basert generering.

## Utbetaling og status fra Helved

[UtbetalingService](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingService.kt) håndterer opprettelse, gjenbruk, sending og retry. De faktiske [enumverdiene](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingStatus.kt) er:

```text
UTKAST → KLAR_TIL_UTBETALING → SENDT_TIL_UTBETALING
       → MOTTATT_AV_UTBETALING → BEHANDLET_AV_UTBETALING
       → UTBETALT eller FEILET
```

Oversikten viser statusene, ikke alle tillatte overganger og retry-forløp.

[UtbetalingKafkaProducer](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingKafkaProducer.kt) sender med `transaksjonsId` som Kafka-nøkkel, venter på senderesultatet og oppdaterer deretter lokal status. Dette dokumenterer ikke en felles DB/Kafka-transaksjon.

[UtbetalingStatusConsumer](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingStatusConsumer.kt) mapper Helved-status til interne verdier. Service-laget ignorerer oppdateringer som ikke går fremover, og oppdateringer etter en endelig status.

Retry av feil betaling finnes via API-/admin-handlinger. Den planlagte [OppgaveGjenopprettingScheduler](../backend/src/main/kotlin/no/nav/historisk/superhelt/oppgave/OppgaveGjenopprettingScheduler.kt) gjelder oppgaver, ikke betalings-retry. Kafka-listenernes runtime retry-/offset-policy og eventuelle rutiner utenfor repoet er ikke verifisert i denne gjennomgangen.

## Kabal, statistikk og hendelser

[KlageService](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/KlageService.kt) sender klager til Kabal med saksnummer som referanse. Kabal-behandlingen er et eget eksternt forløp, ikke automatisk en ny behandling i Superhelt.

[KabalBehandlingEventConsumer](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/kafka/KabalBehandlingEventConsumer.kt) filtrerer på `kilde == APP_NAVN`. [KlageEventService](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/kafka/KlageEventService.kt) knytter hendelser til eksisterende saker, logger dem og kan opprette Gosys-oppgaver.

[KabalEventRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/KabalEventRepository.kt) lagrer event-ID unikt i `REQUIRES_NEW` før videre behandling. Duplikater hoppes over. Registreringen kan derfor være lagret selv om etterfølgende behandling feiler; registrert event-ID betyr ikke i seg selv at hele hendelsen er ferdig behandlet.

[StatistikkService](../backend/src/main/kotlin/no/nav/historisk/superhelt/statistikk/StatistikkService.kt) bygger saksbehandlingsstatistikk fra endringsloggtyper og publiserer til Kafka. `handleEvent()` fanger og logger feil i stedet for å sende dem videre til kalleren.

## Autentisering og tilgangsgrenser

[SecurityConfig](../backend/src/main/kotlin/no/nav/historisk/superhelt/config/SecurityConfig.kt) setter opp en stateless JWT resource server. `/api/**` krever `READ`, `/admin/**` krever rollen `DRIFT`, og øvrige ikke-offentlige ruter krever autentisering. Azure AD og Wonderwall er konfigurert i [Nais-manifestet](../.nais/app/backend.yaml).

[NavJwtAuthenticationConverter](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/authentication/NavJwtAuthenticationConverter.kt) mapper gruppeclaims til roller, tilganger og tema. I tillegg bruker blant annet [SakRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakRepository.kt) metodebaserte kontroller med `@PreAuthorize` og `@PostAuthorize`. Person- og tematilgang kontrolleres gjennom Tilgangsmaskin- og tema-logikken.

[NaisTokenService](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/token/NaisTokenService.kt) bruker Texas til OBO-token med innlogget bruker eller M2M-token som applikasjon. Integrasjonsoppsettene velger interceptorer og tokenform; se [PDL](../backend/src/main/kotlin/no/nav/historisk/superhelt/person/pdl/PdlConfig.kt) og [Kabal](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/config/KabalConfig.kt). Innlogging, person-/tematilgang og utgående tokenhåndtering er forskjellige kontroller.

## Database, drift og tester

[application.yaml](../backend/src/main/resources/application.yaml) leser databasekonfigurasjon fra miljøvariabler og bruker `open-in-view: false` og `ddl-auto: validate`. Flyway-migrasjonene definerer skjemaet. Nais-manifestet beskriver Cloud SQL, Kafka, integrasjonstilgang, health probes, Prometheus og Java auto-instrumentering. Actuator eksponerer health, info og Prometheus.

Backend-integrasjonstester bruker som standard Testcontainers. Når testprosessen ikke har Docker-tilgang, kan profilen `sandkasse` bruke PostgreSQL og Kafka fra den separate teststacken i [compose.sandkasse.yml](../compose.sandkasse.yml). [application-sandkasse.yml](../backend/src/test/resources/application-sandkasse.yml) angir adressene til den eksterne PostgreSQL- og Kafka-instansen. Adressene kan overstyres med miljøvariabler eller Spring-properties.

Aktiver sandkasseprofilen sammen med profilen `junit` via `spring.profiles.include`. [PostgresTestcontainersConfiguration](../backend/src/test/kotlin/no/nav/historisk/superhelt/test/PostgresTestcontainersConfiguration.kt) og [KafkaTestcontainersConfiguration](../backend/src/test/kotlin/no/nav/historisk/superhelt/test/KafkaTestcontainersConfiguration.kt) velger da eksterne tjenester i stedet for Testcontainers. PostgreSQL-oppsettet gir hver Spring-testkontekst et eget databaseskjema og kjører Flyway der. Kafka-oppsettet oppretter testtopicene, og unike meldings-ID-er gjør at senere testkjøringer ikke blander nye Kafka-meldinger med gamle. Tilkoblingsfeil feiler testkjøringen. Testene faller ikke tilbake til Testcontainers.

[SakRepositoryTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/SakRepositoryTest.kt) og [UtbetalingKafkaProducerIntegrationTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingKafkaProducerIntegrationTest.kt) er eksempler på persistens- og Kafka-testing. [README](../README.md#backend-tester-fra-sandkasse-uten-docker) viser hvordan du starter teststacken, overstyrer tilkoblinger og nullstiller testdata.

[SakActionControllerAttesterTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionControllerAttesterTest.kt) kaller den Spring-injiserte controlleren direkte. Den er ikke i seg selv en HTTP-test av binding, validering og sikkerhetsfiltre. [SakControllerRestTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/rest/SakControllerRestTest.kt) bruker derimot `MockMvcTester`.

## Observerte avvik og begrensninger

| Tema | Regel eller domenedefinisjon | Dagens kode |
|------|-----------------------------|-------------|
| Lagdeling | Services orkestrerer brukstilfeller | `SakActionController` validerer og orkestrerer også saksflyt |
| Persistensgrense | JPA-entities holdes innenfor repository-laget | `getSakEntityOrThrow()` brukes også av `EndringsloggService` |
| Trukket | Utfallet gjelder et krav og fører ikke til vedtak | `henleggSak()` setter `FERDIG` og vedtaksresultatet `HENLAGT` på saken og sender brev, men kaller ikke vedtakslagringen |
| Gjenåpning | Oppstart av ny behandling er ikke automatisk revurdering | Statistikken klassifiserer `GJENAPNET_SAK` som `REVURDERING` |
| Flere krav | En søknad kan omfatte flere krav, hvert vedtak gjelder ett krav, og flere vedtak kan underrettes i samme vedtaksbrev | `Sak` og `Vedtak` har ett resultat og ett beløp og ingen kravreferanse |

Avvikene beskriver forskjeller mellom kilder, ikke beslutninger om å endre atferd. Håndteringen av trukket og gjenåpning kan undersøkes i [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt), og statistikkklassifiseringen i `StatistikkService`.
