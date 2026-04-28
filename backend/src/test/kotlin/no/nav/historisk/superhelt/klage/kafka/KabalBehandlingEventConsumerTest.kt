package no.nav.historisk.superhelt.klage.kafka

import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.kabal.model.BehandlingDetaljer
import no.nav.kabal.model.BehandlingEvent
import no.nav.kabal.model.BehandlingEventType
import no.nav.kabal.model.BehandlingFeilregistrertDetaljer
import no.nav.kabal.model.FeilregistrertBehandlingType
import no.nav.kabal.model.KlageUtfall
import no.nav.kabal.model.KlagebehandlingAvsluttetDetaljer
import no.nav.oppgave.OppgaveType
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

@MockedSpringBootTest
class KabalBehandlingEventConsumerTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var kabalProperties: no.nav.historisk.superhelt.klage.config.KabalProperties

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var sakRepository: SakRepository

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService

    @Test
    fun `oppretter VUR_KONS_YTE-oppgave ved KLAGEBEHANDLING_AVSLUTTET for vår sak`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
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

        sendEvent(event)

        val beskrivelseCaptor = argumentCaptor<String>()
        verify(oppgaveService, timeout(2000)).opprettOppgave(
            type = eq(OppgaveType.VUR_KONS_YTE),
            sak = any(),
            beskrivelse = beskrivelseCaptor.capture(),
            behandlesAvApplikasjon = any(),
        )
        assertThat(beskrivelseCaptor.firstValue)
            .contains("MEDHOLD")
            .contains("Klagebehandling avsluttet")
    }

    @Test
    fun `oppretter oppgave ved BEHANDLING_FEILREGISTRERT`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
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

        sendEvent(event)

        val beskrivelseCaptor = argumentCaptor<String>()
        verify(oppgaveService, timeout(2000)).opprettOppgave(
            type = eq(OppgaveType.VUR_KONS_YTE),
            sak = any(),
            beskrivelse = beskrivelseCaptor.capture(),
            behandlesAvApplikasjon = any(),
        )
        assertThat(beskrivelseCaptor.firstValue)
            .contains("feilregistrert")
            .contains("Feil sak")
    }

    @Test
    fun `ignorerer event med annen kilde enn SUPERHELT`() {
        val sak = SakTestData.lagreNySak(
            sakRepository,
            SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
        )

        val event = lagBehandlingEvent(
            kildeReferanse = sak.saksnummer.value,
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

        sendEvent(event)

        // Gir konsumenten litt tid til å evt. prosessere (den skal ignorere)
        Thread.sleep(500)
        verify(oppgaveService, never()).opprettOppgave(any(), any(), any(), any())
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

        sendEvent(event)

        Thread.sleep(500)
        verify(oppgaveService, never()).opprettOppgave(any(), any(), any(), any())
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

    private fun sendEvent(event: BehandlingEvent) {
        val json = objectMapper.writeValueAsString(event)
        val record = ProducerRecord<String, String>(
            kabalProperties.behandlingEventTopic,
            event.kildeReferanse,
            json,
        )
        kafkaTemplate.send(record)
    }
}

