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
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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
class SakshistorikkControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvcTester

    @Autowired
    private lateinit var repository: SakRepository

    @MockitoBean
    private lateinit var infotrygdClient: InfotrygdClient

    @Nested
    @WithSaksbehandler
    inner class Infotrygd {
        private val fnr = FolkeregisterIdent("02345678901")

        @WithSaksbehandler
        @Test
        fun `infotrygd returnerer historikk for person`() {
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

            assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).hasSize(2)
                })
            verify(infotrygdClient).hentHistorikk(eq(fnr))
        }

        @WithSaksbehandler
        @Test
        fun `infotrygd returnerer tom liste når tema er annet enn HEL`() {

            assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HJE)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).isEmpty()
                })
            verify(infotrygdClient, never()).hentHistorikk(any())
        }

        @WithSaksbehandler(tema = [FellesKodeverkTema.HJE])
        @Test
        fun `infotrygd returnerer tom liste når Saksbehandler ikke har tilgang til HEL`() {

            assertGetSakHistorikkPerson(fnr, null)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).isEmpty()
                })
            verify(infotrygdClient, never()).hentHistorikk(any())
        }

        @WithSaksbehandler
        @Test
        fun `infotrygd returnerer tom liste når ingen historikk finnes`() {
            whenever(infotrygdClient.hentHistorikk(any())) doReturn emptyList()
            assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                .satisfies({
                    assertThat(it.infotrygd).isEmpty()
                })
            verify(infotrygdClient).hentHistorikk(eq(fnr))
        }


        @WithMockUser
        @Test
        fun `krever READ-tilgang`() {
            assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HEL)
                .hasStatus(HttpStatus.FORBIDDEN)

        }

    }

    @Nested
    inner class Saker {
        @WithSaksbehandler
        @Nested
        inner class `finn saker for person` {

            @Test
            fun `finn saker for person med filter`() {
                val fnr = FolkeregisterIdent("12345678901")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))
                SakTestData.lagreSak(
                    repository, SakTestData.sakUtenUtbetaling().copy(type = StonadsType.PARYKK, fnr = FolkeregisterIdent("98765432101"))
                )
                SakTestData.lagreSak(
                    repository,
                    SakTestData.sakUtenUtbetaling().copy(type = StonadsType.HOREAPPARAT, fnr = FolkeregisterIdent("98765432101"))
                )

                assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HEL)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson()
                    .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                    .satisfies({
                        assertThat(it.saker).hasSize(2)
                    })
            }


            @Test
            @WithSaksbehandler(tema = [FellesKodeverkTema.HJE])
            fun `finn saker for person uten filter skal filtere på tilgang`() {
                val fnr = FolkeregisterIdent("32345678901")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.HOREAPPARAT))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.HOREAPPARAT))


                assertGetSakHistorikkPerson(fnr, null)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson()
                    .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
                    .satisfies({
                        assertThat(it.saker).hasSize(2)
                        assertThat(it.saker.map { it.tema }).containsOnly(FellesKodeverkTema.HJE)
                    })
            }

            @Test
            @WithSaksbehandler(tema = [FellesKodeverkTema.HJE, FellesKodeverkTema.HEL])
            fun `finn saker for person filterer på tema `() {
                val fnr = FolkeregisterIdent("12345678902")
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.HOREAPPARAT))
                SakTestData.lagreSak(repository, SakTestData.sakUtenUtbetaling().copy(fnr = fnr, type = StonadsType.PARYKK))

                assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HJE)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson()
                    .convertTo(SakshistorikkController.SakshistorikkResponse::class.java)
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
                assertGetSakHistorikkPerson(fnr, FellesKodeverkTema.HEL)
                    .hasStatus(HttpStatus.FORBIDDEN)
            }
        }
    }

    private fun assertGetSakHistorikkPerson(fnr: FolkeregisterIdent, tema: FellesKodeverkTema?) = assertThat(
        mockMvc.get().uri("/api/sakshistorikk/person/{maskertPersonIdent}", fnr.toMaskertPersonIdent())
            .apply { tema?.let { queryParam("tema", it.name) } }
    )
}
