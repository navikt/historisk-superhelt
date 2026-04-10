package no.nav.historisk.superhelt.infotrygd

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.infotrygd.InfotrygdClient
import no.nav.infotrygd.InfotrygdHistorikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.LocalDate

@MockedSpringBootTest
@AutoConfigureMockMvc
class InfotrygdControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @MockitoBean
    private lateinit var infotrygdClient: InfotrygdClient

    private val fnr = FolkeregisterIdent("12345678901")

    // ==================== hentHistorikkForPerson tests ====================

    @WithSaksbehandler
    @Test
    fun `hentHistorikkForPerson returnerer historikk for person`() {
        val historikk = listOf(
            InfotrygdHistorikk(
                dato = LocalDate.of(2024, 1, 15),
                fom = LocalDate.of(2024, 1, 1),
                tom = LocalDate.of(2024, 12, 31),
                tekst = "Ortose",
                kontonummer = "5122000",
                kontonavn = "Ortose",
                belop = "5000",
            ),
            InfotrygdHistorikk(
                dato = LocalDate.of(2023, 6, 1),
                fom = null,
                tom = null,
                tekst = "Parykk",
                kontonummer = "5120000",
                kontonavn = "Parykk",
                belop = "3500",
            )
        )
        whenever(infotrygdClient.hentHistorikk(any())) doReturn historikk

        assertThat(mockMvc.get().uri("/api/infotrygd/historikk/{maskertPersonIdent}", fnr.toMaskertPersonIdent().value))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).hasSize(2)
            })
    }

    @WithSaksbehandler
    @Test
    fun `hentHistorikkForPerson returnerer tom liste når ingen historikk finnes`() {
        whenever(infotrygdClient.hentHistorikk(any())) doReturn emptyList()

        assertThat(mockMvc.get().uri("/api/infotrygd/historikk/{maskertPersonIdent}", fnr.toMaskertPersonIdent().value))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(List::class.java)
            .satisfies({
                assertThat(it).isEmpty()
            })
    }

    @WithSaksbehandler
    @Test
    fun `hentHistorikkForPerson returnerer 404 ved ugyldig maskert personident`() {
        assertThat(mockMvc.get().uri("/api/infotrygd/historikk/{maskertPersonIdent}", "ugyldig-ident"))
            .hasStatus(HttpStatus.NOT_FOUND)
    }

    @WithMockUser
    @Test
    fun `hentHistorikkForPerson krever READ-tilgang`() {
        assertThat(mockMvc.get().uri("/api/infotrygd/historikk/{maskertPersonIdent}", fnr.toMaskertPersonIdent().value))
            .hasStatus(HttpStatus.FORBIDDEN)
    }
}
