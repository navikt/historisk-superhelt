package no.nav.historisk.superhelt.dokarkiv

import no.nav.common.types.NavIdent
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.dokarkiv.rest.JournalforRequest
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.oppgave.OppgaveTestdata
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.test.WithSaksbehandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@WithSaksbehandler
@ExtendWith(SpringExtension::class)
class JournalforServiceTest {

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService
    @MockitoBean
    private lateinit var sakRepository: SakRepository
    @MockitoBean
    private lateinit var endringsloggService: EndringsloggService

    private lateinit var journalforService: JournalforService

    private lateinit var request: JournalforRequest
    private lateinit var jfrOppgave: OppgaveMedSak
    private lateinit var mockSak: Sak

    @BeforeEach
    fun setup() {
        journalforService= JournalforService(
            oppgaveService = oppgaveService,
            sakRepository = sakRepository,
            endringsloggService = endringsloggService
        )

        mockSak = SakTestData.sakUtenUtbetaling()
        jfrOppgave = OppgaveTestdata.oppgaveUtenSak().copy(fnr = mockSak.fnr)
        request = JournalforRequest(
            stonadsType = StonadsType.REISEUTGIFTER,
            jfrOppgaveId = jfrOppgave.oppgaveId,
            bruker = mockSak.fnr,
            avsender = mockSak.fnr,
            dokumenter = listOf(
                JournalforRequest.JournalforDokument(
                    tittel = "Reiseutgifter januar 2024",
                    dokumentInfoId = EksternDokumentInfoId("123"),
                )
            )
        )
        whenever(sakRepository.opprettNySak(any<OpprettSakDto>())).thenReturn(mockSak)
    }

    @Test
    fun `skal opprette ny sak og knytte til oppgave`() {
        val resultat = journalforService.lagNySakOgKnyttDenTilOppgave(request, jfrOppgave)

        assertThat(resultat).isEqualTo(mockSak.saksnummer)
        verify(sakRepository).opprettNySak(any<OpprettSakDto>())
        verify(oppgaveService).knyttOppgaveTilSak(
            saksnummer = mockSak.saksnummer,
            oppgaveId = jfrOppgave.oppgaveId,
            oppgaveType = jfrOppgave.oppgavetype
        )
    }

    @Test
    fun `skal logge dokument mottatt og opprettet sak`() {

        journalforService.lagNySakOgKnyttDenTilOppgave(request, jfrOppgave)

        verify(endringsloggService).logChange(
            eq(mockSak.saksnummer),
            eq(EndringsloggType.DOKUMENT_MOTTATT),
            eq(NavIdent("system")),
            eq("Dokument mottatt av NAV"),
            anyOrNull(),
            any()
        )
        verify(endringsloggService).logChange(
            eq(mockSak.saksnummer),
            eq(EndringsloggType.OPPRETTET_SAK),
            eq(NavIdent("Z999999")),
            eq("Sak opprettet"),
            anyOrNull(),
            any()
        )
    }

    @Test
    fun `skal opprette sak med korrekte verdier`() {
        val opprettSakCaptor = argumentCaptor<OpprettSakDto>()
        journalforService.lagNySakOgKnyttDenTilOppgave(request, jfrOppgave)

        // Then
        verify(sakRepository).opprettNySak(opprettSakCaptor.capture())
        val opprettSakDto = opprettSakCaptor.firstValue

        assertThat(opprettSakDto.type).isEqualTo(request.stonadsType)
        assertThat(opprettSakDto.fnr).isEqualTo(request.bruker)
        assertThat(opprettSakDto.properties?.soknadsDato).isEqualTo(jfrOppgave.opprettetTidspunkt?.toLocalDate())
    }
}