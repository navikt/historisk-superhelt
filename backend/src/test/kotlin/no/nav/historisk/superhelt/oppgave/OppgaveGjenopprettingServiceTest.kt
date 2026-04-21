package no.nav.historisk.superhelt.oppgave

import no.nav.common.types.EksternOppgaveId
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.OppgaveDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class OppgaveGjenopprettingServiceTest {

    private val sakRepository: SakRepository = mock()
    private val oppgaveRepository: OppgaveRepository = mock()
    private val oppgaveClient: OppgaveClient = mock()
    private val oppgaveService: OppgaveService = mock()

    private val service = OppgaveGjenopprettingService(
        sakRepository = sakRepository,
        oppgaveRepository = oppgaveRepository,
        oppgaveClient = oppgaveClient,
        oppgaveService = oppgaveService,
    )

    private val aapneSak = SakTestData.sakMedStatus(SakStatus.UNDER_BEHANDLING)

    @BeforeEach
    fun setup() {
        whenever(sakRepository.finnAapneSaker()).thenReturn(listOf(aapneSak))
    }

    @Test
    fun `skal opprette oppgave når sak ikke har noen oppgaver`() {
        whenever(oppgaveRepository.finnOppgaverForSak(aapneSak.saksnummer)).thenReturn(emptyList())

        val antall = service.gjenopprettManglendeOppgaver()

        assertThat(antall).hasSize(1)
        verify(oppgaveService).opprettOppgave(eq(OppgaveType.BEH_SAK), eq(aapneSak), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `skal opprette oppgave når alle eksisterende oppgaver er ferdigstilt`() {
        val oppgaveId = EksternOppgaveId(42L)
        val ferdigstiltOppgave = OppgaveTestdata.opprettOppgave().copy(status = OppgaveDto.Status.FERDIGSTILT)

        whenever(oppgaveRepository.finnOppgaverForSak(aapneSak.saksnummer)).thenReturn(listOf(oppgaveId))
        whenever(oppgaveClient.hentOppgave(oppgaveId)).thenReturn(ferdigstiltOppgave)

        val antall = service.gjenopprettManglendeOppgaver()

        assertThat(antall).hasSize(1)
        verify(oppgaveService).opprettOppgave(eq(OppgaveType.BEH_SAK), eq(aapneSak), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `skal ikke opprette oppgave når det allerede finnes en åpen oppgave`() {
        val oppgaveId = EksternOppgaveId(42L)
        val aapneOppgave = OppgaveTestdata.opprettOppgave().copy(status = OppgaveDto.Status.OPPRETTET)

        whenever(oppgaveRepository.finnOppgaverForSak(eq(aapneSak.saksnummer), any())).thenReturn(listOf(oppgaveId))
        whenever(oppgaveClient.hentOppgave(oppgaveId)).thenReturn(aapneOppgave)

        val antall = service.gjenopprettManglendeOppgaver()

        assertThat(antall).hasSize(0)
        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `skal anta åpen oppgave og ikke gjenopprette dersom Gosys ikke svarer`() {
        val oppgaveId = EksternOppgaveId(42L)

        whenever(oppgaveRepository.finnOppgaverForSak(eq(aapneSak.saksnummer), any())).thenReturn(listOf(oppgaveId))
        whenever(oppgaveClient.hentOppgave(oppgaveId)).thenThrow(RuntimeException("Gosys nede"))

        val antall = service.gjenopprettManglendeOppgaver()

        assertThat(antall).hasSize(0)
        verify(oppgaveService, never()).opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `skal fortsette til neste sak dersom gjenoppretting feiler`() {
        whenever(oppgaveRepository.finnOppgaverForSak(aapneSak.saksnummer)).thenReturn(emptyList())
        whenever(oppgaveService.opprettOppgave(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(RuntimeException("Uventet feil"))

        val antall = service.gjenopprettManglendeOppgaver()

        assertThat(antall).hasSize(0)
    }
}
