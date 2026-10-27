package no.nav.historisk.superhelt.oppgave.rest

import no.nav.common.consts.APP_NAVN
import no.nav.common.consts.FellesKodeverkTema.HEL
import no.nav.common.consts.FellesKodeverkTema.HJE
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.NavIdent
import no.nav.entraproxy.Enhet
import no.nav.entraproxy.EntraProxyClient
import no.nav.historisk.superhelt.ansatt.NavAnsattTestdata
import no.nav.historisk.superhelt.oppgave.OppgaveMedSak
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.oppgave.OppgaveTestdata
import no.nav.historisk.superhelt.oppgave.gjelderTekst
import no.nav.historisk.superhelt.person.PersonService
import no.nav.historisk.superhelt.person.PersonTestData
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithAttestant
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.oppgave.OppgaveClient
import no.nav.oppgave.OppgaveType
import no.nav.oppgave.model.FinnOppgaverParams
import no.nav.oppgave.model.PatchOppgaveRequest
import no.nav.oppgave.model.SokOppgaverResponse
import no.nav.oppgave.type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
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

    @MockitoBean
    private lateinit var entraProxyClient: EntraProxyClient

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @MockitoBean
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var sakRepository: SakRepository


    @WithSaksbehandler(navIdent = "Z1111111", tema = [HJE, HEL])
    @Test
    fun `Hent oppgaver for saksbehandler filterer ut oppgaver som ikke kan behandles `() {
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(
            antallTreffTotalt = 4,
            oppgaver = listOf(
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "JFR"),
                OppgaveTestdata.opprettOppgave().copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = APP_NAVN),
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

        verify(oppgaveClient).finnOppgaver(check {
            assertThat(it.tema).containsExactlyInAnyOrder(HJE, HEL)
            assertThat(it.tilordnetRessurs?.value).isEqualTo("Z1111111")
            assertThat(it.statuskategori).isEqualTo("AAPEN")
        })

    }

    @WithAttestant(navIdent = "ZATTEST1", tema = [HEL])
    @Test
    fun `finner attesteringsoppgaver per enhet og filtrerer bort egne plukkede og irrelevante oppgaver`() {
        val enhet1 = Enhetsnummer("1234")
        val enhet2 = Enhetsnummer("5678")
        whenever(entraProxyClient.hentEnheter()) doReturn listOf(
            Enhet(enhetnummer = enhet1, navn = "Enhet 1"),
            Enhet(enhetnummer = enhet2, navn = "Enhet 2"),
        )

        val saksbehandler = "ZATTEST1"
        val oppgave1 = attesteringsOppgave(enhet1)
        val oppgaveTilordnetSaksbehandler = attesteringsOppgave(enhet1).copy(tilordnetRessurs = NavIdent(saksbehandler))
        val egenOppgave = attesteringsOppgave(enhet1).copy(opprettetAv = saksbehandler)
        val plukketOppgave = attesteringsOppgave(enhet2).copy(tilordnetRessurs = NavIdent("Z654321"))
        val irrelevantOppgave = attesteringsOppgave(enhet2).copy(behandlesAvApplikasjon = "ANNEN_APP")
        val oppgave2 = attesteringsOppgave(enhet2)

        whenever(oppgaveClient.finnOppgaver(any())).thenAnswer { invocation ->
            when (invocation.getArgument<FinnOppgaverParams>(0).tildeltEnhetsnr) {
                enhet1 -> SokOppgaverResponse(oppgaver = listOf(oppgave1, egenOppgave, oppgaveTilordnetSaksbehandler))
                enhet2 -> SokOppgaverResponse(oppgaver = listOf(plukketOppgave, irrelevantOppgave, oppgave2))
                else -> throw IllegalStateException("Unexpected unit in oppgave search")
            }
        }

        val oppgaver = mockMvc.get().uri("/api/oppgave/attestering")

        assertThat(oppgaver)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(Array<OppgaveMedSak>::class.java)
            .satisfies({
                assertThat(it.map(OppgaveMedSak::oppgaveId)).containsExactly(oppgave1.id, oppgaveTilordnetSaksbehandler.id, oppgave2.id)
            })

        val paramsCaptor = argumentCaptor<FinnOppgaverParams>()
        verify(oppgaveClient, times(2)).finnOppgaver(paramsCaptor.capture())
        assertThat(paramsCaptor.allValues).containsExactly(
            FinnOppgaverParams(
                tildeltEnhetsnr = enhet1,
                oppgavetype = listOf(OppgaveType.GOD_VED.oppgavetype),
                statuskategori = "AAPEN",
                tema = listOf(HEL),
                limit = 100L,
            ),
            FinnOppgaverParams(
                tildeltEnhetsnr = enhet2,
                oppgavetype = listOf(OppgaveType.GOD_VED.oppgavetype),
                statuskategori = "AAPEN",
                tema = listOf(HEL),
                limit = 100L,
            ),
        )
    }

    @WithAttestant(navIdent = "ZATTEST2")
    @Test
    fun `attesteringsoppgaver returnerer tom liste når det ikke er noen oppgaver`() {
        whenever(entraProxyClient.hentEnheter()) doReturn NavAnsattTestdata.createEnheter(1)
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(oppgaver = null)

        assertThat(mockMvc.get().uri("/api/oppgave/attestering"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(Array<OppgaveMedSak>::class.java)
            .satisfies({
                assertThat(it).isEmpty()
            })
        verify(oppgaveClient).finnOppgaver(any())
    }

    @WithSaksbehandler(navIdent = "ZSAKSBEH1", tema = [HEL])
    @Test
    fun `attesteringsoppgaver returnerer tom liste uten attestantrolle`() {
        assertThat(mockMvc.get().uri("/api/oppgave/attestering"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(Array<OppgaveMedSak>::class.java)
            .satisfies({
                assertThat(it).isEmpty()
            })

        verify(oppgaveClient, never()).finnOppgaver(any())
    }

    @WithSaksbehandler(navIdent = "ZATTEST3")
    @Test
    fun `tilordner oppgave til innlogget saksbehandler`() {
        val oppgave = attesteringsOppgave(Enhetsnummer("1234"))
            .copy(versjon = 7)
        val oppdatertOppgave = oppgave.copy(tilordnetRessurs = NavIdent("ZATTEST3"))
        whenever(oppgaveClient.hentOppgave(oppgave.id)) doReturn oppgave
        whenever(oppgaveClient.patchOppgave(eq(oppgave.id), any())) doReturn oppdatertOppgave

        assertThat(mockMvc.patch().uri("/api/oppgave/${oppgave.id}/tilordnet").with(csrf()))
            .hasStatus(HttpStatus.OK)

        val requestCaptor = argumentCaptor<PatchOppgaveRequest>()
        verify(oppgaveClient).patchOppgave(eq(oppgave.id), requestCaptor.capture())
        assertThat(requestCaptor.firstValue.versjon).isEqualTo(7)
        assertThat(requestCaptor.firstValue.tilordnetRessurs).isEqualTo(NavIdent("ZATTEST3"))
    }

    @WithSaksbehandler(navIdent = "ZATTEST3")
    @Test
    fun `tilordner ikke oppgave på nytt når den allerede er tilordnet innlogget saksbehandler`() {
        val oppgave = attesteringsOppgave(Enhetsnummer("1234"))
            .copy(tilordnetRessurs = NavIdent("ZATTEST3"))
        whenever(oppgaveClient.hentOppgave(oppgave.id)) doReturn oppgave

        assertThat(mockMvc.patch().uri("/api/oppgave/${oppgave.id}/tilordnet").with(csrf()))
            .hasStatus(HttpStatus.OK)

        verify(oppgaveClient, never()).patchOppgave(any(), any())
    }

    @WithSaksbehandler(navIdent = "ZATTEST3")
    @Test
    fun `tilordner ikke oppgave når den allerede er tilordnet en annen saksbehandler`() {
        val oppgave = attesteringsOppgave(Enhetsnummer("1234"))
            .copy(tilordnetRessurs = NavIdent("ZANNEN1"))
        whenever(oppgaveClient.hentOppgave(oppgave.id)) doReturn oppgave

        val response = mockMvc.patch().uri("/api/oppgave/${oppgave.id}/tilordnet").with(csrf())

        assertThat(response)
            .hasStatus(HttpStatus.CONFLICT)
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .convertTo(ProblemDetail::class.java)
            .satisfies({
                assertThat(it.status).isEqualTo(HttpStatus.CONFLICT.value())
            })

        verify(oppgaveClient, never()).patchOppgave(any(), any())
    }

    @WithSaksbehandler(navIdent = "ZATTEST3")
    @Test
    fun `tilordner ikke oppgave som ikke finnes`() {
        val oppgaveId = OppgaveTestdata.opprettOppgave().id
        whenever(oppgaveClient.hentOppgave(oppgaveId)) doReturn null

        assertThat(mockMvc.patch().uri("/api/oppgave/$oppgaveId/tilordnet").with(csrf()))
            .hasStatus(HttpStatus.NOT_FOUND)

        verify(oppgaveClient, never()).patchOppgave(any(), any())
    }

    @WithSaksbehandler(navIdent = "Z123456", tema = [HEL])
    @Test
    fun `Hent oppgaver for person henter oppgaver for andre applikasjoner også`() {
//        whenever(entraProxyClient.hentTema()) doReturn setOf("HEL")
        val fnr = FolkeregisterIdent("12345678901")
        whenever(personService.hentPerson(fnr)) doReturn PersonTestData.testPerson.copy(fnr = fnr)
        whenever(oppgaveClient.finnOppgaver(any())) doReturn SokOppgaverResponse(
            antallTreffTotalt = 4,
            oppgaver = listOf(
                OppgaveTestdata.opprettOppgave(fnr.value).copy(oppgavetype = "JFR"),
                OppgaveTestdata.opprettOppgave(fnr.value).copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = APP_NAVN),
                OppgaveTestdata.opprettOppgave(fnr.value, tema = HJE).copy(oppgavetype = "VUR", behandlesAvApplikasjon = APP_NAVN),
                OppgaveTestdata.opprettOppgave(fnr.value).copy(oppgavetype = "BEH_SAK", behandlesAvApplikasjon = "other")
            )
        )

        assertThat(mockMvc.get().uri("/api/oppgave/person/{maskertPersonident}", fnr.toMaskertPersonIdent().value))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(3)
            })

        verify(oppgaveClient).finnOppgaver(check {
            assertThat(it.tema).containsExactlyInAnyOrder( HEL)
            assertThat(it.tilordnetRessurs).isNull()
            assertThat(it.statuskategori).isEqualTo("AAPEN")
            assertThat(it.aktoerId).isNotNull()

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
                assertThat(it.oppgaveGjelderTekst).isEqualTo(oppgave.gjelderTekst)
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
        val sak = SakTestData.lagreSak(sakRepository, SakTestData.sakUtenUtbetaling())
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

    private fun attesteringsOppgave(enhet: Enhetsnummer) =
        OppgaveTestdata.opprettOppgave().copy(
            tildeltEnhetsnr = enhet,
            tilordnetRessurs = null,
            oppgavetype = OppgaveType.GOD_VED.oppgavetype,
            behandlesAvApplikasjon = APP_NAVN,
        )
}
