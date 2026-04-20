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
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import no.nav.kabal.model.SendSakV4Request
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
        // Kabal returnerer ingen body – Unit/void er default for mockede metoder
    }

    @Nested
    @WithSaksbehandler
    inner class `saksbehandler med WRITE-tilgang` {

        @Test
        fun `sender klage til Kabal og returnerer 204 når sak er ferdig`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val request = gyldigKlageRequest()

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.NO_CONTENT)

            verify(kabalClient).sendSakV4(any())

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg).anySatisfy { innslag ->
                assertThat(innslag.type).isEqualTo(EndringsloggType.KLAGE_SENDT_KABAL)
                assertThat(innslag.beskrivelse).contains("FTRL_10_7I")
            }
        }

        @Test
        fun `sender korrekt payload til Kabal`() {
            val datoKlageMottatt = LocalDate.now().minusDays(10)
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest(datoKlageMottatt)))
                .hasStatus(HttpStatus.NO_CONTENT)

            val captor = argumentCaptor<SendSakV4Request>()
            verify(kabalClient).sendSakV4(captor.capture())
            val request = captor.firstValue

            assertThat(request.type.name).isEqualTo("KLAGE")
            assertThat(request.fagsak.fagsystem).isEqualTo("HJELPEMIDLER")
            assertThat(request.fagsak.fagsakId).isEqualTo(sak.saksnummer.value)
            assertThat(request.kildeReferanse).isEqualTo(sak.saksnummer.value)
            assertThat(request.dvhReferanse).isEqualTo(sak.saksnummer.value)
            assertThat(request.hjemler).containsExactly("FTRL_10_7I")
            assertThat(request.ytelse).isEqualTo("HEL_HEL")
            assertThat(request.brukersKlageMottattVedtaksinstans).isEqualTo(datoKlageMottatt)
            assertThat(request.sakenGjelder.id.verdi).isEqualTo(sak.fnr.value)
            assertThat(request.klager.id.verdi).isEqualTo(sak.fnr.value)
        }

        @Test
        fun `kommentar inkluderes i payload til Kabal og i endringslogg`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            val kommentar = "Klager er uenig i vedtaket"
            val request = mapOf(
                "hjemmelId" to "FTRL_10_7I",
                "datoKlageMottatt" to LocalDate.now().minusDays(5).toString(),
                "kommentar" to kommentar,
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), request))
                .hasStatus(HttpStatus.NO_CONTENT)

            val captor = argumentCaptor<SendSakV4Request>()
            verify(kabalClient).sendSakV4(captor.capture())
            assertThat(captor.firstValue.kommentar).isEqualTo(kommentar)

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg).anySatisfy { innslag ->
                assertThat(innslag.type).isEqualTo(EndringsloggType.KLAGE_SENDT_KABAL)
                assertThat(innslag.beskrivelse).contains(kommentar)
            }
        }

        @Test
        fun `skriver ikke endringslogg når Kabal-kallet feiler`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )
            whenever(kabalClient.sendSakV4(any())) doThrow HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Kabal er utilgjengelig",
                org.springframework.http.HttpHeaders.EMPTY,
                """{"feil":"Kabal er utilgjengelig"}""".toByteArray(),
                null
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest()))
                .hasStatus(HttpStatus.BAD_GATEWAY)

            val endringslogg = endringsloggService.findBySak(sak.saksnummer)
            assertThat(endringslogg).noneSatisfy { innslag ->
                assertThat(innslag.type).isEqualTo(EndringsloggType.KLAGE_SENDT_KABAL)
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
            whenever(kabalClient.sendSakV4(any())) doThrow HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Kabal er utilgjengelig",
                org.springframework.http.HttpHeaders.EMPTY,
                """{"feil":"Kabal er utilgjengelig"}""".toByteArray(),
                null
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

    @Nested
    @WithMockJwtAuth(roles = [Role.ATTESTANT], permissions = [Permission.READ, Permission.WRITE])
    inner class `attestant kan ikke sende klage` {

        @Test
        fun `returnerer 400 når attestant forsøker å sende klage (mangler SEND_KLAGE-rettighet)`() {
            val sak = SakTestData.lagreNySak(
                sakRepository,
                SakTestData.nySakCompleteUtbetaling(sakStatus = SakStatus.FERDIG)
            )

            assertThat(sendKlage(sak.saksnummer.value.toString(), gyldigKlageRequest()))
                .hasStatus(HttpStatus.BAD_REQUEST)

            verifyNoInteractions(kabalClient)
        }
    }


    // ── helpers ───────────────────────────────────────────────────────────────

    private fun gyldigKlageRequest(datoKlageMottatt: LocalDate = LocalDate.now().minusDays(10)): Map<String, String> = mapOf(
        "hjemmelId" to "FTRL_10_7I",
        "datoKlageMottatt" to datoKlageMottatt.toString(),
    )

    private fun sendKlage(saksnummer: String, body: Any): MockMvcTester.MockMvcRequestBuilder =
        mockMvc.post().uri("/api/sak/{saksnummer}/klage", saksnummer)
            .with(csrf())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(body))
}
