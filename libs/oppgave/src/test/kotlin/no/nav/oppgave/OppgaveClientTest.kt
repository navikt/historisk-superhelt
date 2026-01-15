package no.nav.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import no.nav.oppgave.model.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

class OppgaveClientTest {

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private val oppgaveClient = OppgaveClient(restClient)

    private val oppgave = Oppgave(
        id = 123456789,
        tildeltEnhetsnr = Enhetsnummer("4100"),
        tema = "OPP",
        oppgavetype = "JFR",
        versjon = 1,
        prioritet = Oppgave.Prioritet.NORM,
        status = Oppgave.Status.OPPRETTET,
        aktivDato = LocalDate.now()
    )
    // ==================== opprettOppgave tests ====================

    @Test
    fun `opprettOppgave returnerer opprettet oppgave`() {
        // Arrange
        val request = OpprettOppgaveRequest(
            personident = "12345678901",
            tema = "OPP",
            oppgavetype = "JFR"
        )

        mockServer.expect(requestTo("/api/v1/oppgaver"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.personident").value("12345678901"))
            .andExpect(jsonPath("$.tema").value("OPP"))
            .andExpect(jsonPath("$.oppgavetype").value("JFR"))
            .andRespond(
                withStatus(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(oppgave))
            )

        // Act
        oppgaveClient.opprettOppgave(request)

        // Assert
        mockServer.verify()
    }

    // ==================== hentOppgave tests ====================

    @Test
    fun `hentOppgave returnerer oppgave`() {
        // Arrange
        val oppgaveId = 123456789L

        val oppgave = Oppgave(
            id = oppgaveId,
            tildeltEnhetsnr = Enhetsnummer("4100"),
            tema = "OPP",
            oppgavetype = "JFR",
            versjon = 1,
            prioritet = Oppgave.Prioritet.NORM,
            status = Oppgave.Status.AAPNET,
            aktivDato = LocalDate.now()
        )

        mockServer.expect(requestTo("/api/v1/oppgaver/$oppgaveId"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(oppgave))
            )

        // Act
        oppgaveClient.hentOppgave(oppgaveId)

        // Assert
        mockServer.verify()
    }

    // ==================== patchOppgave tests ====================

    @Test
    fun `patchOppgave oppdaterer oppgave`() {
        // Arrange
        val oppgaveId = 123456789L
        val request = PatchOppgaveRequest(
            versjon = 1,
            status = Oppgave.Status.FERDIGSTILT
        )

        mockServer.expect(requestTo("/api/v1/oppgaver/$oppgaveId"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.versjon").value(1))
            .andExpect(jsonPath("$.status").value("FERDIGSTILT"))
            .andExpect(jsonPath("$.beskrivelse").doesNotExist())
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(oppgave))
            )

        // Act
        oppgaveClient.patchOppgave(oppgaveId, request)

        // Assert
        mockServer.verify()
    }

    @Test
    fun `patchOppgave kaster exception ved konflikt (409 Conflict)`() {
        // Arrange
        val oppgaveId = 123456789L
        val request = PatchOppgaveRequest(
            versjon = 1,
            status = Oppgave.Status.FERDIGSTILT
        )

        mockServer.expect(requestTo("/api/v1/oppgaver/$oppgaveId"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(jsonPath("$.versjon").value(1))
            .andExpect(jsonPath("$.status").value("FERDIGSTILT"))
            .andRespond(withStatus(HttpStatus.CONFLICT))

        // Act & Assert
        assertThatThrownBy {
            oppgaveClient.patchOppgave(oppgaveId, request)
        }.isInstanceOf(RuntimeException::class.java)
        mockServer.verify()
    }

    // ==================== finnOppgaver tests ====================

    @Test
    fun `finnOppgaver returnerer liste med oppgaver`() {
        // Arrange
        val params = FinnOppgaverParams(
            statuskategori = "AAPEN",
            tema = listOf("OPP"),
            limit = 10
        )

        val oppgaver = listOf(
            oppgave.copy(id = 1),
            oppgave.copy(id = 2),
        )

        val response = SokOppgaverResponse(
            antallTreffTotalt = 2,
            oppgaver = oppgaver
        )

        mockServer.expect(requestTo("/api/v1/oppgaver?statuskategori=AAPEN&tema=OPP&limit=10"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("statuskategori", "AAPEN"))
            .andExpect(queryParam("tema", "OPP"))
            .andExpect(queryParam("limit", "10"))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        oppgaveClient.finnOppgaver(params)

        // Assert
        mockServer.verify()
    }

    @Test
    fun `finnOppgaver med flere søkeparametere returnerer filtrerte oppgaver`() {
        // Arrange
        val params = FinnOppgaverParams(
            statuskategori = "AAPEN",
            tema = listOf("OPP", "FOR"),
            oppgavetype = listOf("JFR", "KONT"),
            tildeltEnhetsnr = Enhetsnummer("4100"),
            tilordnetRessurs = NavIdent("Z999999"),
            limit = 20,
            offset = 0
        )

        val response = SokOppgaverResponse(
            antallTreffTotalt = 1,
            oppgaver = listOf(
                oppgave
            )
        )

        mockServer.expect(requestTo("/api/v1/oppgaver?statuskategori=AAPEN&tema=OPP&tema=FOR&oppgavetype=JFR&oppgavetype=KONT&tildeltEnhetsnr=4100&tilordnetRessurs=Z999999&limit=20&offset=0"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("statuskategori", "AAPEN"))
            .andExpect(queryParam("tema", "OPP", "FOR"))
            .andExpect(queryParam("oppgavetype", "JFR", "KONT"))
            .andExpect(queryParam("tildeltEnhetsnr", "4100"))
            .andExpect(queryParam("tilordnetRessurs", "Z999999"))
            .andExpect(queryParam("limit", "20"))
            .andExpect(queryParam("offset", "0"))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        oppgaveClient.finnOppgaver(params)

        // Assert
        mockServer.verify()
    }
}
