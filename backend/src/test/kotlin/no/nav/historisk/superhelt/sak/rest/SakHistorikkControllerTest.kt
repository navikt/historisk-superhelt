package no.nav.historisk.superhelt.sak.rest

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakTestData
import no.nav.historisk.superhelt.test.MockedSpringBootTest
import no.nav.historisk.superhelt.test.WithSaksbehandler
import no.nav.infotrygd.InfotrygdClient
import no.nav.infotrygd.InfotrygdHistorikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
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
class SakHistorikkControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var repository: SakRepository

    @MockitoBean
    private lateinit var infotrygdClient: InfotrygdClient

    @Nested
    inner class Infotrygd {
        private val fnr = FolkeregisterIdent("02345678901")

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

            mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakHistorikkController.SakHistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).hasSize(2)
                })
        }

        @WithSaksbehandler
        @Test
        fun `hentHistorikkForPerson returnerer tom liste når tema er annet enn HEL`() {
            val historikk = listOf(
                InfotrygdHistorikk(
                    dato = LocalDate.of(2024, 1, 15),
                    fom = LocalDate.of(2024, 1, 1),
                    tom = LocalDate.of(2024, 12, 31),
                    tekst = "Ortose",
                    kontonummer = "5122000",
                    kontonavn = "Ortose",
                    belop = "5000",
                )

            )
            whenever(infotrygdClient.hentHistorikk(any())) doReturn historikk

            mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HJE)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakHistorikkController.SakHistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).isEmpty()
                })
        }

        @WithSaksbehandler
        @Test
        fun `hentHistorikkForPerson returnerer tom liste når ingen historikk finnes`() {
            whenever(infotrygdClient.hentHistorikk(any())) doReturn emptyList()
            mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakHistorikkController.SakHistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).isEmpty()
                })
        }


        @WithMockUser
        @Test
        fun `hentHistorikkForPerson krever READ-tilgang`() {
            mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.FORBIDDEN)

        }

    }

    inner class Saker {
        @WithSaksbehandler
        @Nested
        inner class `finn saker for person` {

            @Test
            fun `finn saker for person ok`() {
                val fnr = FolkeregisterIdent("12345678901")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = FolkeregisterIdent("98765432101")))

                mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HEL)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson()
                    .convertTo(SakHistorikkController.SakHistorikkResponse::class.java)
                    .satisfies({
                        assertThat(it.saker).hasSize(2)
                    })

            }

            @Test
            @WithSaksbehandler(tema = [FellesKodeverkTema.HJE, FellesKodeverkTema.HEL])
            fun `finn saker for person filterer på tema `() {
                val fnr = FolkeregisterIdent("12345678902")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.HOREAPPARAT))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))

                mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HJE)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson()
                    .convertTo(SakHistorikkController.SakHistorikkResponse::class.java)
                    .satisfies({
                        assertThat(it.saker).hasSize(1)
                        assertThat(it.saker.first().type).isEqualTo(StonadsType.HOREAPPARAT)
                    })
            }

            @WithMockUser()
            @Test
            fun `finn saker for saksbehandler uten lesetilgang skal gi feil`() {
                val fnr = FolkeregisterIdent("22345678901")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr))
                mvcAssertSakhistorikk(fnr, FellesKodeverkTema.HEL)
                    .hasStatus(HttpStatus.FORBIDDEN)
            }
        }
    }

    private fun mvcAssertSakhistorikk(fnr: FolkeregisterIdent, tema: FellesKodeverkTema) = assertThat(
        mockMvc.get().uri(
            "/api/sakhistorikk//person/{maskertPersonIdent}/{tema}", fnr.toMaskertPersonIdent(), tema
        )
    )
}
