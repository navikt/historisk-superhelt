package no.nav.kabal

import no.nav.kabal.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import java.time.LocalDateTime

class KabalClientTest {

    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .build()

    private val restTemplate: RestTemplate = RestTemplate()
    private lateinit var mockServer: MockRestServiceServer

    private lateinit var kabalClient: KabalClient

    @BeforeEach
    fun setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build()
        val restClient = RestClient.builder(restTemplate).build()
        kabalClient = KabalClient(restClient)
    }

    // ==================== sendSakV4-tester ====================

    @Test
    fun `sendSakV4 should send sak successfully`() {
        // Forbered
        val request = createValidSendSakV4Request()

        mockServer.expect(requestTo("/api/oversendelse/v4/sak"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.type").value("KLAGE"))
            .andExpect(jsonPath("$.klager.id.verdi").value("12345678901"))
            .andRespond(withStatus(HttpStatus.OK))

        // Utfør
        kabalClient.sendSakV4(request)

        // Verifiser
        mockServer.verify()
    }

    @Test
    fun `sendSakV4 should include all optional fields when provided`() {
        // Forbered
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            prosessfullmektig = Prosessfullmektig(
                id = Ident(IdentType.PERSON, "98765432101"),
                navn = "Advokat Hansen",
                adresse = Adresse(
                    adresselinje1 = "Storgata 10",
                    postnummer = "0157",
                    poststed = "Oslo"
                )
            ),
            fagsak = Fagsak("123456", "K9"),
            kildeReferanse = "ref-123",
            dvhReferanse = "dvh-456",
            hjemler = listOf(Hjemmel.FVL_11.id, Hjemmel.FVL_12.id),
            forrigeBehandlendeEnhet = "NAV Oslo",
            tilknyttedeJournalposter = listOf(
                TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-123"),
                TilknyttetJournalpost(JournalpostType.OPPRINNELIG_VEDTAK, "jp-456")
            ),
            brukersKlageMottattVedtaksinstans = LocalDate.of(2026, 3, 1),
            frist = LocalDate.of(2026, 6, 1),
            sakMottattKaTidspunkt = LocalDateTime.of(2026, 3, 5, 10, 0),
            ytelse = "OMS_OMP",
            kommentar = "Klager er uenig i vedtaket",
            hindreAutomatiskSvarbrev = true,
            saksbehandlerIdentForTildeling = "Z123456"
        )

        mockServer.expect(requestTo("/api/oversendelse/v4/sak"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.prosessfullmektig.navn").value("Advokat Hansen"))
            .andExpect(jsonPath("$.kildeReferanse").value("ref-123"))
            .andExpect(jsonPath("$.hjemler[0]").value("FVL_11"))
            .andExpect(jsonPath("$.kommentar").value("Klager er uenig i vedtaket"))
            .andRespond(withStatus(HttpStatus.OK))

        // Utfør
        kabalClient.sendSakV4(request)

        // Verifiser
        mockServer.verify()
    }

    @Test
    fun `sendSakV4 should handle VIRKSOMHET ident type`() {
        // Forbered
        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.VIRKSOMHET, "987654321")),
            klager = Klager(Ident(IdentType.VIRKSOMHET, "987654321")),
            fagsak = Fagsak("654321", "K9"),
            kildeReferanse = "kilde-ref-virksomhet",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
        )

        mockServer.expect(requestTo("/api/oversendelse/v4/sak"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.sakenGjelder.id.type").value("VIRKSOMHET"))
            .andRespond(withStatus(HttpStatus.OK))

        // Utfør
        kabalClient.sendSakV4(request)

        // Verifiser
        mockServer.verify()
    }

    @Test
    fun `sendSakV4 should handle multiple journalposter`() {
        // Forbered
        val journalposter = listOf(
            TilknyttetJournalpost(JournalpostType.BRUKERS_KLAGE, "jp-111"),
            TilknyttetJournalpost(JournalpostType.OPPRINNELIG_VEDTAK, "jp-222"),
            TilknyttetJournalpost(JournalpostType.OVERSENDELSESBREV, "jp-333"),
            TilknyttetJournalpost(JournalpostType.ANNET, "jp-444")
        )

        val request = SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "K9"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
            tilknyttedeJournalposter = journalposter
        )

        mockServer.expect(requestTo("/api/oversendelse/v4/sak"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.tilknyttedeJournalposter.length()").value(4))
            .andRespond(withStatus(HttpStatus.OK))

        // Utfør
        kabalClient.sendSakV4(request)

        // Verifiser
        mockServer.verify()
    }

    // ==================== Hjelpemetoder ====================

    private fun createValidSendSakV4Request(): SendSakV4Request {
        return SendSakV4Request(
            type = SakType.KLAGE,
            sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, "12345678901")),
            klager = Klager(Ident(IdentType.PERSON, "12345678901")),
            fagsak = Fagsak("123456", "K9"),
            kildeReferanse = "kilde-ref-123",
            forrigeBehandlendeEnhet = "4201",
            ytelse = "HEL_HEL",
        )
    }
}
