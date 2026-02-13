package no.nav.historisk.superhelt.oppgave.rest

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.oppgave.OppgaveTestdata
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.person.PersonTestData
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.gjelder
import no.nav.oppgave.model.SokOppgaverResponse
import no.nav.oppgave.type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper

@MockedSpringBootTest
@AutoConfigureMockMvc
class OppgaveControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var oppgaveClient: OppgaveClient

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @MockitoBean
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var sakRepository: SakRepository

    @WithSaksbehandler(navIdent = "Z123456")
    @Test
    fun `Hent oppgaver for saksbehandler`() {
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(
            antallTreffTotalt = 4,
            oppgaver = listOf(
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "JFR"),
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = "SUPERHELT"),
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = null),
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = "other")
            )
        )

        assertThat(mockMvc.get().uri("/api/oppgave/saksbehandler"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(2)
            })
    }

    @WithSaksbehandler(navIdent = "Z123456")
    @Test
    fun `Hent oppgaver for person`() {
        val fnr = FolkeregisterIdent("12345678901")
        whenever(personService.hentPerson(fnr)) doReturn PersonTestData.testPerson.copy(fnr = fnr)
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(
            antallTreffTotalt = 4,
            oppgaver = listOf(
                OppgaveTestdata.opprettOppgave(fnr.value),
                OppgaveTestdata.opprettOppgave(fnr.value),
                OppgaveTestdata.opprettOppgave(fnr.value),
                OppgaveTestdata.opprettOppgave(fnr.value)
            )
        )

        assertThat(mockMvc.get().uri("/api/oppgave/person/{maskertPersonident}", fnr.toMaskertPersonIdent().value))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(4)
            })
    }

    @WithSaksbehandler
    @Test
    fun `hent oppgave uten sak`() {
        val oppgave = OppgaveTestdata.opprettOppgave()
        whenever(oppgaveClient.hentOppgave(any())) doReturn oppgave

        assertThat(mockMvc.get().uri("/api/oppgave/${oppgave.id}"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(OppgaveMedSak::class.java)
            .satisfies({
                assertThat(it.saksnummer).isNull()
                assertThat(it.sakStatus).isNull()
                assertThat(it.oppgaveId).isEqualTo(oppgave.id)
                assertThat(it.fnr.value).isEqualTo(oppgave.bruker?.ident)
                assertThat(it.oppgavestatus).isEqualTo(oppgave.status)
                assertThat(it.oppgavetype.oppgavetype).isEqualTo(oppgave.oppgavetype)
                assertThat(it.oppgaveGjelder).isEqualTo(oppgave.gjelder)
                assertThat(it.journalpostId).isEqualTo(oppgave.journalpostId)
                assertThat(it.tilordnetRessurs).isEqualTo(oppgave.tilordnetRessurs)
                assertThat(it.beskrivelse).isEqualTo(oppgave.beskrivelse)
                assertThat(it.fristFerdigstillelse).isEqualTo(oppgave.fristFerdigstillelse)
                assertThat(it.behandlesAvApplikasjon).isEqualTo(oppgave.behandlesAvApplikasjon)
                assertThat(it.tildeltEnhetsnr).isEqualTo(oppgave.tildeltEnhetsnr)
                assertThat(it.opprettetAv).isEqualTo(oppgave.opprettetAv)
                assertThat(it.maskertPersonIdent).isNotNull()
            })
    }

    @WithSaksbehandler
    @Test
    fun `hent oppgave med sak`() {
        val oppgave = OppgaveTestdata.opprettOppgave()
        val sak = SakTestData.lagreNySak(sakRepository)
        oppgaveService.knyttOppgaveTilSak(
            saksnummer = sak.saksnummer,
            oppgaveId = oppgave.id,
            oppgaveType = oppgave.type
        )

        whenever(oppgaveClient.hentOppgave(any())) doReturn oppgave

        assertThat(mockMvc.get().uri("/api/oppgave/${oppgave.id}"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(OppgaveMedSak::class.java)
            .satisfies({
                assertThat(it.oppgaveId).isEqualTo(oppgave.id)
                assertThat(it.saksnummer).isEqualTo(sak.saksnummer)
                assertThat(it.sakStatus).isEqualTo(sak.status)

            })
    }
}