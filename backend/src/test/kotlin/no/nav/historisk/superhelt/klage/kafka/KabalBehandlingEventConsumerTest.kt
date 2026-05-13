package no.nav.historisk.superhelt.klage.kafka

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SecurityContextUtils
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.withMockedUser
import no.nav.kabal.model.BehandlingDetaljer
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import no.nav.kabal.model.BehandlingFeilregistrertDetaljer
import no.nav.kabal.model.FeilregistrertBehandlingType
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.KlagebehandlingAvsluttetDetaljer
import no.nav.oppgave.OppgaveType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @Autowired
    private lateinit var klageEventService: KlageEventService

    @Autowired
    private lateinit var kabalBehandlingEventConsumer: KabalBehandlingEventConsumer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService

    private val systemPermissions = listOf(Permission.READ, Permission.WRITE)

    @Test
    fun `oppretter VUR_KONS_YTE-oppgave ved KLAGEBEHANDLING_AVSLUTTET for vår sak`() {
        val sak = SakTestData.lagreSak(
            repository = sakRepository,
            sak = SakTestData.sakMedStatus(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
            type = BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = BehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = KlageUtfall.MEDHOLD,
                    journalpostReferanser = listOf("jp-111"),
                )
            )
        )

        SecurityContextUtils.runAsSystemuser("test", systemPermissions) {
            klageEventService.behandleEvent(event)
        }

        val beskrivelseCaptor = argumentCaptor<String>()
        verify(oppgaveService).opprettOppgave(
            type = eq(OppgaveType.VUR_KONS_YTE),
            sak = any(),
            beskrivelse = beskrivelseCaptor.capture(),
            tilordneTil = anyOrNull(),
            behandlesAvApplikasjon = anyOrNull(),
            journalpostId = anyOrNull(),
        )
        assertThat(beskrivelseCaptor.firstValue)
            .contains("MEDHOLD")
            .contains("Klagebehandling avsluttet")
    }

    @Test
    fun `oppretter VUR_KONS_YTE-oppgave med årsak ved BEHANDLING_FEILREGISTRERT uten å endre saksstatus`() {
        val sak = SakTestData.lagreSak(
            repository = sakRepository,
            sak = SakTestData.sakMedStatus(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
            type = BehandlingEventType.BEHANDLING_FEILREGISTRERT,
            detaljer = BehandlingDetaljer(
                behandlingFeilregistrert = BehandlingFeilregistrertDetaljer(
                    feilregistrert = LocalDateTime.now(),
                    navIdent = "Z123456",
                    reason = "Feil sak",
                    type = FeilregistrertBehandlingType.KLAGE,
                )
            )
        )

        SecurityContextUtils.runAsSystemuser("test", systemPermissions) {
            klageEventService.behandleEvent(event)
        }

        val beskrivelseCaptor = argumentCaptor<String>()
        verify(oppgaveService).opprettOppgave(
            type = eq(OppgaveType.VUR_KONS_YTE),
            sak = any(),
            beskrivelse = beskrivelseCaptor.capture(),
            tilordneTil = anyOrNull(),
            behandlesAvApplikasjon = anyOrNull(),
            journalpostId = anyOrNull(),
        )
        assertThat(beskrivelseCaptor.firstValue)
            .contains("feilregistrert")
            .contains("Feil sak")

        // Saksbehandler beslutter selv – status skal ikke endres automatisk
        val oppdatertSak = withMockedUser { sakRepository.getSak(sak.saksnummer) }
        assertThat(oppdatertSak.status).isEqualTo(SakStatus.FERDIG)

        val endringslogg = withMockedUser { endringsloggService.findBySak(sak.saksnummer) }
        assertThat(endringslogg).anyMatch { it.type == EndringsloggType.KABAL_BEHANDLING_FEILREGISTRERT }
    }

    @Test
    fun `ignorerer event med annen kilde enn SUPERHELT`() {
        val event = lagBehandlingEvent(
            kildeReferanse = "SH-000001",
            kilde = "ANNET_FAGSYSTEM",
            type = BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = BehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = KlageUtfall.STADFESTELSE,
                    journalpostReferanser = emptyList(),
                )
            )
        )

        // Kilde-filtrering skjer i KabalBehandlingEventConsumer — kall consumer direkte
        val record = ConsumerRecord("kabal.behandling-events-junit", 0, 0L, event.kildeReferanse, objectMapper.writeValueAsString(event))
        kabalBehandlingEventConsumer.onBehandlingEvent(record)

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `ignorerer event der sak ikke finnes`() {
        val event = lagBehandlingEvent(
            kildeReferanse = "SH-999999",
            type = BehandlingEventType.KLAGEBEHANDLING_AVSLUTTET,
            detaljer = BehandlingDetaljer(
                klagebehandlingAvsluttet = KlagebehandlingAvsluttetDetaljer(
                    avsluttet = LocalDateTime.now(),
                    utfall = KlageUtfall.AVVIST,
                    journalpostReferanser = emptyList(),
                )
            )
        )

        SecurityContextUtils.runAsSystemuser("test", systemPermissions) {
            klageEventService.behandleEvent(event)
        }

        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun lagBehandlingEvent(
        kildeReferanse: String,
        kilde: String = "SUPERHELT",
        type: BehandlingEventType,
        detaljer: BehandlingDetaljer,
    ) = BehandlingEvent(
        eventId = UUID.randomUUID(),
        kildeReferanse = kildeReferanse,
        kilde = kilde,
        kabalReferanse = UUID.randomUUID().toString(),
        type = type,
        detaljer = detaljer,
    )
}
