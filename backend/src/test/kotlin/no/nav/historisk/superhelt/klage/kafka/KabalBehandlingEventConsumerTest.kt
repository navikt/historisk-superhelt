package no.nav.historisk.superhelt.klage.kafka

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSystemUser
import no.nav.kabal.model.AnkeITrygderettenUtfall
import no.nav.kabal.model.AnkeITrygderettenbehandlingOpprettetDetaljer
import no.nav.kabal.model.KabalBehandlingDetaljer
import no.nav.kabal.model.KabalBehandlingEvent
import no.nav.kabal.model.KabalBehandlingEventType
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.KlagebehandlingAvsluttetDetaljer
import no.nav.oppgave.OppgaveType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

@MockedSpringBootTest
class KabalBehandlingEventConsumerTest {

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var endringsloggService: EndringsloggService

    @Autowired
    private lateinit var klageEventService: KlageEventService

    @Autowired
    private lateinit var kabalBehandlingEventConsumer: KabalBehandlingEventConsumer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService

    @WithSystemUser(permissions = [Permission.READ, Permission.WRITE])
    @ParameterizedTest(name = "should create oppgave when utfall is {0}")
    @EnumSource(value = KlageUtfall::class, names = ["MEDHOLD", "DELVIS_MEDHOLD", "OPPHEVET", "UGUNST", "RETUR"])
    fun `should create oppgave for utfall that requires action`(utfall: KlageUtfall) {
        val sak = SakTestData.lagreSak(
            repository = sakRepository,
            sak = SakTestData.sakMedStatus(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
            type = KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = KabalBehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = utfall,
                    journalpostReferanser = listOf("jp-111"),
                )
            )
        )

        klageEventService.behandleEvent(event)

        val beskrivelseCaptor = argumentCaptor<String>()
        verify(oppgaveService).opprettOppgave(
            type = eq(OppgaveType.VUR_KONS_YTE),
            sak = any(),
            beskrivelse = beskrivelseCaptor.capture(),
            tilordneTil = anyOrNull(),
            behandlesAvApplikasjon = anyOrNull(),
            journalpostId = anyOrNull(),
        )
        verify(endringsloggService).logChange(
            saksnummer = eq(sak.saksnummer),
            endringsType = any(),
            navBruker = anyOrNull(),
            endring = any(),
            beskrivelse = anyOrNull(),
            tidspunkt = any(),
        )
    }

    @WithSystemUser(permissions = [Permission.READ, Permission.WRITE])
    @ParameterizedTest(name = "should only log to endringslogg when utfall is {0}")
    @EnumSource(value = KlageUtfall::class, names = ["TRUKKET", "STADFESTELSE", "AVVIST", "HENLAGT"])
    fun `should not create oppgave for utfall that does not require action`(utfall: KlageUtfall) {
        val sak = SakTestData.lagreSak(
            repository = sakRepository,
            sak = SakTestData.sakMedStatus(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
            type = KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = KabalBehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = utfall,
                    journalpostReferanser = emptyList(),
                )
            )
        )

        klageEventService.behandleEvent(event)

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        verify(endringsloggService).logChange(
            saksnummer = eq(sak.saksnummer),
            endringsType = any(),
            navBruker = anyOrNull(),
            endring = any(),
            beskrivelse = anyOrNull(),
            tidspunkt = any(),
        )
    }

    @Test
    fun `should ignore event from other source than SUPERHELT`() {
        val event = lagBehandlingEvent(
            kildeReferanse = "SH-000001",
            kilde = "ANNET_FAGSYSTEM",
            type = KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = KabalBehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = KlageUtfall.STADFESTELSE,
                    journalpostReferanser = emptyList(),
                )
            )
        )

        val record = ConsumerRecord("kabal.behandling-events-junit", 0, 0L, event.kildeReferanse, objectMapper.writeValueAsString(event))
        kabalBehandlingEventConsumer.onBehandlingEvent(record)

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @WithSystemUser(permissions = [Permission.READ, Permission.WRITE])
    @Test
    fun `should throw exception when sak does not exist`() {
        val event = lagBehandlingEvent(
            kildeReferanse = "SH-999999",
            type = KabalBehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = KabalBehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = KlageUtfall.AVVIST,
                    journalpostReferanser = emptyList(),
                )
            )
        )

        assertThatThrownBy {
            klageEventService.behandleEvent(event)
        }.isInstanceOf(IkkeFunnetException::class.java)

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    @WithSystemUser(permissions = [Permission.READ, Permission.WRITE])
    @Test
    fun `should not create oppgave for ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET even when utfall has lagOppgave true`() {
        // DELVIS_MEDHOLD har lagOppgave=true, men ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET
        // skal ikke trigge opprettelse av oppgave via utfall-logikken (skalLageOppgaveVedUtfall=false).
        // Uten guard ville dette krasjet fordi utfallOgAvsluttet() returnerer null for OPPRETTET-events.
        val sak = SakTestData.lagreSak(
            repository = sakRepository,
            sak = SakTestData.sakMedStatus(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
            type = KabalBehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
            detaljer = KabalBehandlingDetaljer(
                ankeITrygderettenbehandlingOpprettet = AnkeITrygderettenbehandlingOpprettetDetaljer(
                    sendtTilTrygderetten = LocalDateTime.now(),
                    utfall = AnkeITrygderettenUtfall.DELVIS_MEDHOLD,
                )
            )
        )

        klageEventService.behandleEvent(event)

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    private fun lagBehandlingEvent(
        kildeReferanse: String,
        kilde: String = "SUPERHELT",
        type: KabalBehandlingEventType,
        detaljer: KabalBehandlingDetaljer,
    ) = KabalBehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = kilde,
        kabalReferanse = UUID.randomUUID().toString(),
        type = type,
        detaljer = detaljer,
    )
}
