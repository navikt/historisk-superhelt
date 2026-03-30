package no.nav.historisk.superhelt.klage.rest

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithMockJwtAuth
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.historisk.superhelt.test.bodyAsProblemDetail
import no.nav.kabal.KabalClient
import no.nav.kabal.model.SendSakV4Response
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@MockedSpringBootTest
@AutoConfigureMockMvc
class KlageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var sakRepository: SakRepository

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @MockitoBean
    private lateinit var tilgangsmaskinService: TilgangsmaskinService

    @MockitoBean
    private lateinit var kabalClient: KabalClient

    @BeforeEach
    fun setup() {
        whenever(tilgangsmaskinService.sjekkKomplettTilgang(any())) doReturn TilgangsmaskinClient.TilgangResult(
            harTilgang = true
        )
        whenever(kabalClient.sendSakV4(any())) doReturn SendSakV4Response(
            behandlingId = "test-behandling-id",
            mottattDato = "2026-03-30",
        )
    }

    @Nested
    @WithSaksbehandler
    inner class `saksbehandler med WRITE-tilgang` {

        @Test
        fun `sender klage til Kabal og returnerer 200 når sak er ferdig`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val request = gyldigKlageRequest()

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.behandlingId") { assertThat(it).isEqualTo("test-behandling-id") }
                .hasPathSatisfying("$.mottattDato") { assertThat(it).isEqualTo("2026-03-30") }

            verify(kabalClient).sendSakV4(any())

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg).anySatisfy { innslag ->
                assertThat(innslag.type).isEqualTo(EndringsloggType.KLAGE_SENDT_KABAL)
                assertThat(innslag.beskrivelse).contains("FTRL_10_7I")
            }
        }

        @Test
        fun `returnerer 400 når sak ikke har SEND_KLAGE-rettighet (sak er under behandling)`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.UNDER_BEHANDLING)
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyAsProblemDetail()
                .satisfies({ assertThat(it?.detail).isNotBlank() })

            verifyNoInteractions(kabalClient)
        }

        @Test
        fun `returnerer 400 ved ugyldig hjemmelId`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val request = mapOf(
                "hjemmelId" to "UKJENT_HJEMMEL_XYZ",
                "datoKlageMottatt" to "2026-01-15",
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyAsProblemDetail()
                .satisfies({ assertThat(it?.detail).isNotBlank() })

            verifyNoInteractions(kabalClient)
        }

        @Test
        fun `returnerer 400 når hjemmelId mangler i request`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val request = mapOf(
                "datoKlageMottatt" to "2026-01-15",
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.BAD_REQUEST)

            verifyNoInteractions(kabalClient)
        }

        @Test
        fun `returnerer 400 når datoKlageMottatt mangler i request`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val request = mapOf(
                "hjemmelId" to "FTRL_10_7I",
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.BAD_REQUEST)

            verifyNoInteractions(kabalClient)
        }

        @Test
        fun `returnerer 404 når sak ikke finnes`() {
            assertThat(sendKlage("Mock-99999", gyldigKlageRequest()))
                .hasStatus(HttpStatus.NOT_FOUND)

            verifyNoInteractions(kabalClient)
        }

        @Test
        fun `returnerer 502 når Kabal-kallet feiler`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            whenever(kabalClient.sendSakV4(any())) doThrow no.nav.kabal.KabalException(
                message = "Feil fra Kabal API: HTTP 503",
                statusCode = 503,
                responseBody = """{"feil":"Kabal er utilgjengelig"}""",
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest()))
                .hasStatus(HttpStatus.BAD_GATEWAY)
                .bodyAsProblemDetail()
                .satisfies({
                    assertThat(it?.detail).contains("Kabal")
                })
        }
    }

    @Nested
    /** Saksbehandler-rolle men kun READ (ikke WRITE) – @PreAuthorize('WRITE') på service skal gi 403 */
    @WithMockJwtAuth(roles = [Role.SAKSBEHANDLER], permissions = [Permission.READ])
    inner class `saksbehandler uten WRITE-tilgang` {

        @Test
        fun `returnerer 403 ved forsøk på sending uten WRITE-tilgang`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest()))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyAsProblemDetail()
                .satisfies({ assertThat(it?.detail).isNotBlank() })

            verifyNoInteractions(kabalClient)
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun gyldigKlageRequest(): Map<String, String> = mapOf(
        "hjemmelId" to "FTRL_10_7I",
        "datoKlageMottatt" to LocalDate.now().minusDays(10).toString(),
    )

    private fun sendKlage(saksnummer: String, body: Any): MockMvcTester.MockMvcRequestBuilder =
        mockMvc.post().uri("/api/sak/{saksnummer}/klage", saksnummer)
            .with(csrf())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(body))
}

