package no.nav.historisk.superhelt.dokarkiv.rest

import net.datafaker.Faker
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.dokarkiv.DokarkivService
import no.nav.historisk.superhelt.dokarkiv.DokarkivTestdata
import no.nav.historisk.superhelt.dokarkiv.JournalforService
import no.nav.historisk.superhelt.dokarkiv.JournalpostService
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.oppgave.OppgaveTestdata
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveType
import no.nav.saf.graphql.JournalStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class DokarkivJournalforControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var journalpostService: JournalpostService

    @MockitoBean
    private lateinit var journalforService: JournalforService

    @MockitoBean
    private lateinit var dokArkivService: DokarkivService

    @MockitoBean
    private lateinit var oppgaveService: OppgaveService

    @MockitoBean
    private lateinit var sakRepository: SakRepository

    private val faker = Faker()

    @WithSaksbehandler
    @Test
    fun `skal journalføre dokument og opprett ny sak`() {
        val journalPost = DokarkivTestdata.journalPost().copy(journalstatus = JournalStatus.UNDER_ARBEID)
        val journalpostId = journalPost.journalpostId
        val jfrOppgaveId = EksternOppgaveId(faker.number().positive().toLong())
        val sak = SakTestData.sakUtenUtbetaling()
        val saksnummer = sak.saksnummer
        val oppgave =
            OppgaveTestdata.oppgaveUtenSak().copy(oppgavetype = OppgaveType.JFR, journalpostId = journalpostId)

        whenever(journalpostService.hentJournalpost(any())).thenReturn(journalPost)
        whenever(oppgaveService.getOppgave(any())).thenReturn(oppgave)
        whenever(journalforService.lagNySakOgKnyttDenTilOppgave(any(), any())).thenReturn(saksnummer)
        whenever(sakRepository.getSak(any())).thenReturn(sak)

        val request = JournalforRequest(
            stonadsType = faker.options().option(StonadsType::class.java),
            jfrOppgaveId = jfrOppgaveId,
            bruker = FolkeregisterIdent(faker.numerify("###########")),
            avsender = FolkeregisterIdent(faker.numerify("###########")),
            dokumenter = listOf(
                JournalforRequest.JournalforDokument(
                    tittel = faker.lorem().sentence(),
                    dokumentInfoId = EksternDokumentInfoId(faker.numerify("###########")),
                )
            )
        )

        assertThat(
            mockMvc.put()
                .uri("/api/dokarkiv/{journalpostId}/journalfor", journalpostId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatusOk()
            .bodyText()
            .satisfies({
                assertThat(it).isEqualTo(saksnummer.value)
            })

        verify(journalforService).lagNySakOgKnyttDenTilOppgave(any(), any())
        verify(dokArkivService).journalførIArkivet(
            journalPostId = eq(journalpostId),
            fagsaksnummer = eq(saksnummer),
            journalfoerendeEnhet = any(),
            request = any()
        )
        verify(oppgaveService).ferdigstillOppgave(eq(jfrOppgaveId))
        verify(oppgaveService).opprettOppgave(eq(OppgaveType.BEH_SAK), eq(sak), any(), eq(sak.saksbehandler.navIdent))
    }

    @WithSaksbehandler
    @Test
    fun `skal journalføre dokument og ikke opprette ny sak`() {
        val journalPost = DokarkivTestdata.journalPost().copy(journalstatus = JournalStatus.UNDER_ARBEID)
        val journalpostId = journalPost.journalpostId
        val jfrOppgaveId = EksternOppgaveId(faker.number().positive().toLong())
        val sak = SakTestData.sakUtenUtbetaling()
        val saksnummer = sak.saksnummer
        val oppgave = OppgaveTestdata.oppgaveUtenSak().copy(
            oppgavetype = OppgaveType.JFR,
            journalpostId = journalpostId,
            saksnummer = saksnummer,
            sakStatus = SakStatus.UNDER_BEHANDLING
        )

        whenever(journalpostService.hentJournalpost(any())).thenReturn(journalPost)
        whenever(oppgaveService.getOppgave(any())).thenReturn(oppgave)
//        whenever(journalforService.lagNySakOgKnyttDenTilOppgave(any(), any())).thenReturn(saksnummer)
        whenever(sakRepository.getSak(any())).thenReturn(sak)

        val request = JournalforRequest(
            stonadsType = faker.options().option(StonadsType::class.java),
            jfrOppgaveId = jfrOppgaveId,
            bruker = FolkeregisterIdent(faker.numerify("###########")),
            avsender = FolkeregisterIdent(faker.numerify("###########")),
            dokumenter = listOf(
                JournalforRequest.JournalforDokument(
                    tittel = faker.lorem().sentence(),
                    dokumentInfoId = EksternDokumentInfoId(faker.numerify("###########")),
                )
            )
        )

        assertThat(
            mockMvc.put()
                .uri("/api/dokarkiv/{journalpostId}/journalfor", journalpostId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .hasStatusOk()
            .bodyText()
            .satisfies({
                assertThat(it).isEqualTo(saksnummer.value)
            })

        verify(journalforService, never()).lagNySakOgKnyttDenTilOppgave(any(), any())
        verify(dokArkivService).journalførIArkivet(
            journalPostId = eq(journalpostId),
            fagsaksnummer = eq(saksnummer),
            journalfoerendeEnhet = any(),
            request = any()
        )
    }
}
