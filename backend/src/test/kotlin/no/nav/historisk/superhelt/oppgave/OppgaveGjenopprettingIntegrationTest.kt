package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.SakTestData.sakMedUtbetaling
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import no.nav.oppgave.model.OpprettOppgaveRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@MockedSpringBootTest
@Transactional
class OppgaveGjenopprettingIntegrationTest {

    @Autowired
    private lateinit var scheduler: OppgaveGjenopprettingService

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var oppgaveRepository: OppgaveRepository

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @MockitoBean
    private lateinit var oppgaveClient: OppgaveClient

    private val oppgaveDto = OppgaveTestdata.opprettOppgave()
        .copy(status = OppgaveDto.Status.OPPRETTET)

    @BeforeEach
    fun setup() {
        whenever(oppgaveClient.opprettOppgave(any())) doReturn oppgaveDto
    }

    @Test
    fun `sak uten oppgaver skal få ny oppgave`() {
        val sak = SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING))
        scheduler.gjenopprettManglendeOppgaver()

        val captor = argumentCaptor<OpprettOppgaveRequest>()
        verify(oppgaveClient).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.saksreferanse).isEqualTo(sak.saksnummer.value)
        assertThat(captor.firstValue.oppgavetype).isEqualTo(OppgaveType.BEH_SAK.oppgavetype)
    }

    @Test
    fun `sak med åpen oppgave skal ikke få ny oppgave`() {
        val sak = SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING))
        // Knytt en eksisterende åpen oppgave til saken
        val eksisterendeOppgaveId = EksternOppgaveId(2001L)
        withOppgaveService { oppgaveService.knyttOppgaveTilSak(sak.saksnummer, eksisterendeOppgaveId, OppgaveType.BEH_SAK) }
        whenever(oppgaveClient.hentOppgave(eksisterendeOppgaveId)) doReturn
            OppgaveTestdata.opprettOppgave().copy(status = OppgaveDto.Status.OPPRETTET)

        scheduler.gjenopprettManglendeOppgaver()

        verify(oppgaveClient, never()).opprettOppgave(any())
    }

    @Test
    fun `sak med lukket oppgave skal få ny oppgave`() {
        val sak = SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING))
        // Knytt en eksisterende åpen oppgave til saken
        val eksisterendeOppgaveId = EksternOppgaveId(5001L)
        withOppgaveService { oppgaveService.knyttOppgaveTilSak(sak.saksnummer, eksisterendeOppgaveId, OppgaveType.BEH_SAK) }
        whenever(oppgaveClient.hentOppgave(eksisterendeOppgaveId)) doReturn
            OppgaveTestdata.opprettOppgave().copy(status = OppgaveDto.Status.FERDIGSTILT)

        scheduler.gjenopprettManglendeOppgaver()

        verify(oppgaveClient).opprettOppgave(any())
    }

    @Test
    fun `sak med ferdigstilt oppgave skal få ny oppgave`() {
        val sak = SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.TIL_ATTESTERING))
        val ferdigstiltOppgaveId = EksternOppgaveId(3001L)
        withOppgaveService { oppgaveService.knyttOppgaveTilSak(sak.saksnummer, ferdigstiltOppgaveId, OppgaveType.GOD_VED) }
        whenever(oppgaveClient.hentOppgave(ferdigstiltOppgaveId)) doReturn
            OppgaveTestdata.opprettOppgave().copy(status = OppgaveDto.Status.FERDIGSTILT)

        scheduler.gjenopprettManglendeOppgaver()

        val captor = argumentCaptor<OpprettOppgaveRequest>()
        verify(oppgaveClient).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.saksreferanse).isEqualTo(sak.saksnummer.value)
        assertThat(captor.firstValue.oppgavetype).isEqualTo(OppgaveType.GOD_VED.oppgavetype)
    }

    @Test
    fun `ferdige saker skal ikke behandles`() {
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.FERDIG))
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.FEILREGISTRERT))

        scheduler.gjenopprettManglendeOppgaver()

        verify(oppgaveClient, never()).opprettOppgave(any())
    }

    @Test
    fun `bare åpne saker blant flere skal behandles`() {
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.UNDER_BEHANDLING))
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.TIL_ATTESTERING))
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.FERDIG))
        SakTestData.lagreSak(sakRepository, sakMedUtbetaling().copy(status = SakStatus.FEILREGISTRERT))

        scheduler.gjenopprettManglendeOppgaver()

        verify(oppgaveClient, times(2)).opprettOppgave(any())
    }


    /** Utfører en handling med saksbehandler-tillatelser — for testoppsett utenfor selve jobben. */
    private fun <T> withOppgaveService(block: () -> T): T =
        no.nav.historisk.superhelt.test.withMockedUser { block() }
}
